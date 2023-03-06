package app.bbr.minardebug.util

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.rounded() = roundTo(3)

private fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}
