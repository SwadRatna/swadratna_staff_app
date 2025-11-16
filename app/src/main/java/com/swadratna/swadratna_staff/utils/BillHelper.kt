package com.swadratna.swadratna_staff.utils

data class TwoLineName(
    val line1: String,
    val line2: String?
)

fun formatItemName24(original: String, maxLine: Int = 12): TwoLineName {
    val clean = original.trim().replace(Regex("\\s+"), " ")

    if (clean.length <= maxLine) {
        return TwoLineName(
            line1 = clean,
            line2 = null
        )
    }

    if (clean.length <= maxLine * 2) {
        val words = clean.split(" ")

        val line1Words = mutableListOf<String>()
        var length = 0

        for (w in words) {
            val extra = if (line1Words.isEmpty()) w.length else w.length + 1
            if (length + extra > maxLine) break
            line1Words += w
            length += extra
        }

        val line1 = line1Words.joinToString(" ")
        val line2 = words.drop(line1Words.size).joinToString(" ")

        return TwoLineName(
            line1 = line1,
            line2 = if (line2.isBlank()) null else line2
        )
    }

    val words = clean.split(" ")

    fun trunc(words: List<String>, keep: Int): List<String> {
        return words.map { w ->
            if (w.length <= keep) w else w.take(keep) + "."
        }
    }

    for (keep in 6 downTo 2) {
        val t = trunc(words, keep)
        val joined = t.joinToString(" ")

        if (joined.length <= maxLine * 2) {
            val line1 = joined.take(maxLine)
            val rest = joined.drop(maxLine)
            val line2 = rest.take(maxLine)

            return TwoLineName(
                line1 = line1.trimEnd(),
                line2 = if (line2.isBlank()) null else line2.trimEnd()
            )
        }
    }

    val initials = words.joinToString(" ") { it.first() + "." }
    val line1 = initials.take(maxLine)
    val line2 = initials.drop(maxLine).take(maxLine)

    return TwoLineName(
        line1 = line1.trimEnd(),
        line2 = if (line2.isBlank()) null else line2.trimEnd()
    )
}
