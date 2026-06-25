package com.kam666.mealplanner.data.remote

object MercadonaCategoryMap {
    val appKeyToIds: Map<String, List<Int>> = mapOf(
        "veg"       to listOf(29),
        "fruit"     to listOf(27),
        "meat"      to listOf(38, 37, 40, 42, 44),
        "dairy"     to listOf(72, 75, 54, 56, 77),
        "frozen"    to listOf(145, 147, 148, 149, 150),
        "pantry"    to listOf(112, 118, 120, 121, 122, 126),
        "condiment" to listOf(115, 116, 117),
        "drinks"    to listOf(156, 158, 159, 162)
    )
}
