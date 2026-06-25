package com.kam666.mealplanner.data.local.excel

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ExcelImportData(
    val recipes: List<RecipeImport>,
    val ingredients: List<IngredientImport>
)

data class RecipeImport(val name: String)

data class IngredientImport(
    val recipeName: String,
    val name: String,
    val type: String,
    val quantity: Float,
    val unit: String
)

@Singleton
class ExcelParser @Inject constructor() {

    fun parse(stream: InputStream): ExcelImportData {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(stream.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) entries[entry.name] = zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        Log.d("ExcelParser", "ZIP entries: ${entries.keys.sorted()}")
        val sharedStrings = parseSharedStrings(entries["xl/sharedStrings.xml"] ?: byteArrayOf())
        val sheetPaths = resolveSheetPaths(entries).mapKeys { it.key.trim().lowercase() }
        Log.d("ExcelParser", "sheetPaths: $sheetPaths")

        // New flat format: single "Recetas" sheet (Receta | Ingrediente | Categoria | Cantidad | Unidad)
        // Legacy format: "Platos" + "Ingredientes" sheets
        return if (sheetPaths.containsKey("recetas")) {
            val rows = parseSheet(entries[sheetPaths["recetas"]!!] ?: byteArrayOf(), sharedStrings)
                .drop(1) // skip header
            val ingredients = rows.mapNotNull { cols ->
                val recipeName = cols.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val ingName    = cols.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val type       = cols.getOrNull(2)?.trim() ?: ""
                val quantity   = cols.getOrNull(3)?.toFloatOrNull() ?: 1f
                val unit       = cols.getOrNull(4)?.trim() ?: ""
                IngredientImport(recipeName.trim(), ingName.trim(), type, quantity, unit)
            }
            val recipes = ingredients.map { it.recipeName }.distinct().map { RecipeImport(it) }
            ExcelImportData(recipes, ingredients)
        } else {
            // Legacy format
            val recipes = sheetPaths["platos"]?.let { path ->
                parseRecipes(entries[path] ?: byteArrayOf(), sharedStrings)
            } ?: emptyList()
            val ingredients = sheetPaths["ingredientes"]?.let { path ->
                parseIngredients(entries[path] ?: byteArrayOf(), sharedStrings)
            } ?: emptyList()
            ExcelImportData(recipes, ingredients)
        }
    }

    private fun resolveSheetPaths(entries: Map<String, ByteArray>): Map<String, String> {
        val NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

        // workbook.xml: sheet name → rId
        val nameToRId = mutableMapOf<String, String>()
        entries["xl/workbook.xml"]?.let { bytes ->
            val parser = newParser(bytes)
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    val localName = parser.name.substringAfterLast(':')
                    if (localName == "sheet") {
                        // Try namespace-aware lookup first, then fallback to iterating all attributes
                        val name = (parser.getAttributeValue(null, "name")
                            ?: attrByLocalName(parser, "name") ?: "").trim()
                        val rId = parser.getAttributeValue(NS_R, "id")
                            ?: attrByLocalName(parser, "id")
                            ?: ""
                        Log.d("ExcelParser", "sheet element: name='$name' rId='$rId'")
                        if (name.isNotEmpty() && rId.isNotEmpty()) nameToRId[name] = rId
                    }
                }
                event = parser.next()
            }
        }
        Log.d("ExcelParser", "nameToRId: $nameToRId")

        // workbook.xml.rels: rId → target path (normalized to ZIP-root-relative, no leading slash)
        val rIdToTarget = mutableMapOf<String, String>()
        entries["xl/_rels/workbook.xml.rels"]?.let { bytes ->
            val parser = newParser(bytes)
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    val localName = parser.name.substringAfterLast(':')
                    if (localName == "Relationship") {
                        val id = parser.getAttributeValue(null, "Id")
                            ?: attrByLocalName(parser, "Id") ?: ""
                        val raw = parser.getAttributeValue(null, "Target")
                            ?: attrByLocalName(parser, "Target") ?: ""
                        val normalized = if (raw.startsWith("/")) raw.removePrefix("/") else "xl/$raw"
                        Log.d("ExcelParser", "Relationship: Id='$id' normalized='$normalized'")
                        if (id.isNotEmpty()) rIdToTarget[id] = normalized
                    }
                }
                event = parser.next()
            }
        }
        Log.d("ExcelParser", "rIdToTarget: $rIdToTarget")

        return nameToRId.mapValues { (_, rId) -> rIdToTarget[rId] ?: "" }.filterValues { it.isNotEmpty() }
    }

    private fun attrByLocalName(parser: XmlPullParser, localName: String): String? {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i).substringAfterLast(':') == localName) {
                return parser.getAttributeValue(i)
            }
        }
        return null
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        if (bytes.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        val parser = newParser(bytes)
        var inT = false
        var text = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> if (parser.name == "t") { inT = true; text = StringBuilder() }
                XmlPullParser.TEXT -> if (inT) text.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "t" -> inT = false
                    "si" -> result.add(text.toString())
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseSheet(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        if (bytes.isEmpty()) return emptyList()
        val rows = mutableListOf<List<String>>()
        val parser = newParser(bytes)
        var currentRow = mutableListOf<String>()
        var inRow = false
        var cellType: String? = null
        var cellCol = -1
        var cellValue = StringBuilder()
        var inValue = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> { inRow = true; currentRow = mutableListOf() }
                    "c" -> {
                        val ref = parser.getAttributeValue(null, "r") ?: ""
                        cellCol = colIndex(ref)
                        cellType = parser.getAttributeValue(null, "t")
                        cellValue = StringBuilder()
                    }
                    "v", "t" -> inValue = true
                }
                XmlPullParser.TEXT -> if (inValue) cellValue.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v", "t" -> inValue = false
                    "c" -> {
                        if (cellCol >= 0) {
                            while (currentRow.size <= cellCol) currentRow.add("")
                            currentRow[cellCol] = when (cellType) {
                                "s" -> sharedStrings.getOrElse(cellValue.toString().trim().toIntOrNull() ?: -1) { "" }
                                else -> cellValue.toString().trim()
                            }
                        }
                    }
                    "row" -> { if (inRow) { rows.add(currentRow.toList()); inRow = false } }
                }
            }
            event = parser.next()
        }
        return rows
    }

    private fun parseRecipes(bytes: ByteArray, sharedStrings: List<String>): List<RecipeImport> {
        val rows = parseSheet(bytes, sharedStrings)
        return rows.drop(1) // skip header
            .mapNotNull { cols -> cols.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { RecipeImport(it.trim()) } }
    }

    private fun parseIngredients(bytes: ByteArray, sharedStrings: List<String>): List<IngredientImport> {
        val rows = parseSheet(bytes, sharedStrings)
        return rows.drop(1) // skip header
            .mapNotNull { cols ->
                val recipeName = cols.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val ingName = cols.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val type = cols.getOrNull(2)?.trim() ?: ""
                val quantity = cols.getOrNull(3)?.toFloatOrNull() ?: 1f
                val unit = cols.getOrNull(4)?.trim() ?: ""
                IngredientImport(recipeName.trim(), ingName.trim(), type, quantity, unit)
            }
    }

    private fun colIndex(ref: String): Int {
        var col = 0
        for (c in ref) {
            if (c.isLetter()) col = col * 26 + (c.uppercaseChar() - 'A' + 1)
            else break
        }
        return col - 1
    }

    private fun newParser(bytes: ByteArray): XmlPullParser =
        Xml.newPullParser().apply { setInput(bytes.inputStream(), "UTF-8") }
}
