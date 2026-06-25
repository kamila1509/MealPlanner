package com.kam666.mealplanner.domain.usecase.backup

import com.kam666.mealplanner.data.local.dao.IngredientDao
import com.kam666.mealplanner.data.local.dao.RecipeDao
import com.kam666.mealplanner.data.local.dao.RecipeIngredientDao
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class ExportExcelUseCase @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {
    suspend operator fun invoke(): ByteArray {
        val recipes = recipeDao.getAllOnce().associateBy { it.id }
        val ingredients = ingredientDao.getAllOnce().associateBy { it.id }
        val links = recipeIngredientDao.getAllOnce()

        // Build rows: one per recipe-ingredient link, sorted by recipe name then ingredient name
        data class Row(
            val recipeName: String,
            val ingredientName: String,
            val category: String,
            val quantity: Double,
            val unit: String
        )

        val rows = links.mapNotNull { link ->
            val recipe = recipes[link.recipeId] ?: return@mapNotNull null
            val ingredient = ingredients[link.ingredientId] ?: return@mapNotNull null
            Row(
                recipeName = recipe.name,
                ingredientName = ingredient.name,
                category = ingredient.supermarketCategory ?: "",
                quantity = link.quantity,
                unit = ingredient.unit
            )
        }.sortedWith(compareBy({ it.recipeName }, { it.ingredientName }))

        return buildXlsx(rows.map { listOf(it.recipeName, it.ingredientName, it.category, it.quantity.toString(), it.unit) })
    }

    private fun buildXlsx(dataRows: List<List<Any>>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putEntry("[Content_Types].xml", contentTypesXml())
            zip.putEntry("_rels/.rels", relsXml())
            zip.putEntry("xl/workbook.xml", workbookXml())
            zip.putEntry("xl/_rels/workbook.xml.rels", workbookRelsXml())
            zip.putEntry("xl/styles.xml", stylesXml())
            zip.putEntry("xl/worksheets/sheet1.xml", sheetXml(dataRows))
        }
        return out.toByteArray()
    }

    private fun ZipOutputStream.putEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun colLetter(col: Int): String {
        var n = col + 1
        val sb = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            sb.insert(0, ('A' + rem))
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    private fun cellRef(col: Int, row: Int) = "${colLetter(col)}$row"

    private fun sheetXml(dataRows: List<List<Any>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        // Column widths
        sb.append("""<cols>""")
        sb.append("""<col min="1" max="1" width="30" customWidth="1"/>""")
        sb.append("""<col min="2" max="2" width="25" customWidth="1"/>""")
        sb.append("""<col min="3" max="3" width="20" customWidth="1"/>""")
        sb.append("""<col min="4" max="4" width="12" customWidth="1"/>""")
        sb.append("""<col min="5" max="5" width="15" customWidth="1"/>""")
        sb.append("""</cols>""")
        sb.append("""<sheetData>""")

        // Header row (style index 1 = bold)
        val headers = listOf("Receta", "Ingrediente", "Categoria", "Cantidad", "Unidad")
        sb.append("""<row r="1">""")
        headers.forEachIndexed { col, header ->
            sb.append("""<c r="${cellRef(col, 1)}" t="inlineStr" s="1"><is><t>${escape(header)}</t></is></c>""")
        }
        sb.append("""</row>""")

        // Data rows
        dataRows.forEachIndexed { rowIdx, cols ->
            val rowNum = rowIdx + 2
            sb.append("""<row r="$rowNum">""")
            cols.forEachIndexed { col, value ->
                val strVal = value.toString()
                val ref = cellRef(col, rowNum)
                // Column D (index 3) is quantity — emit as number if valid
                if (col == 3) {
                    val num = strVal.toDoubleOrNull()
                    if (num != null) {
                        sb.append("""<c r="$ref"><v>${num.toBigDecimal().stripTrailingZeros().toPlainString()}</v></c>""")
                    } else {
                        sb.append("""<c r="$ref" t="inlineStr"><is><t>${escape(strVal)}</t></is></c>""")
                    }
                } else {
                    sb.append("""<c r="$ref" t="inlineStr"><is><t>${escape(strVal)}</t></is></c>""")
                }
            }
            sb.append("""</row>""")
        }

        sb.append("""</sheetData></worksheet>""")
        return sb.toString()
    }

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbookXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Recetas" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private fun workbookRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun stylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/><diagonal/></border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
  </cellXfs>
</styleSheet>"""
}
