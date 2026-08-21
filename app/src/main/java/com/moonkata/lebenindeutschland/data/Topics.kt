package com.moonkata.lebenindeutschland.data

/**
 * Static registry of the 10 general topics + 16 Bundesland "topics". Small and fixed, so this
 * is a plain object instead of a Room table — see plan.md Phase 1 log.
 */
object Topics {
    val generalNames: Map<String, String> = mapOf(
        "geschichte" to "Geschichte",
        "politik" to "Politik",
        "recht" to "Recht",
        "staat" to "Staat",
        "gesellschaft_familie" to "Gesellschaft und Familie",
        "europa_welt" to "Europa und Welt",
        "bund_laender" to "Bund und Länder",
        "religion_kultur" to "Religion und Kultur",
        "bildung_arbeit" to "Bildung und Arbeit",
        "wirtschaft" to "Wirtschaft",
    )

    val bundeslandNames: Map<String, String> = mapOf(
        "BW" to "Baden-Württemberg",
        "BY" to "Bayern",
        "BE" to "Berlin",
        "BB" to "Brandenburg",
        "HB" to "Bremen",
        "HH" to "Hamburg",
        "HE" to "Hessen",
        "MV" to "Mecklenburg-Vorpommern",
        "NI" to "Niedersachsen",
        "NW" to "Nordrhein-Westfalen",
        "RP" to "Rheinland-Pfalz",
        "SL" to "Saarland",
        "SN" to "Sachsen",
        "ST" to "Sachsen-Anhalt",
        "SH" to "Schleswig-Holstein",
        "TH" to "Thüringen",
    )

    fun displayName(topicId: String): String =
        generalNames[topicId] ?: topicId.removePrefix("bundesland_").uppercase()
}
