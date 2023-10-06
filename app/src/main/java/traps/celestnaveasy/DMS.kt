package traps.celestnaveasy

import androidx.compose.runtime.mutableStateOf

class DMS(
    degParam: String = "",
    minParam: String = "",
    positive: Boolean = true,
) {

    val degStr = mutableStateOf(degParam)
    val minStr = mutableStateOf(minParam)
    val positive = mutableStateOf(positive)
    var deg = 0
    var min = 0.0
    var decimalValue = 0.0

    private var valid = false

    fun validate(): Boolean {
        valid = false
        try {
            deg = degStr.value.toInt()
            min = minStr.value.toDouble()
        } catch (e: Exception) {
            return false
        }
        if (deg<0 || min<0.0 || deg>=90 || min>=60.0) return false
        if (!positive.value) deg = -deg
        decimalValue = Calc.sexaToDecimal(deg, min)
        valid = true
        return true
    }

    fun isValid() = valid

    fun toDegMinString(): String {
        if (!valid) return "invalid"
        return "${deg}° %.1f".format(min)
    }

    fun toDecimalString(): String {
        if (!valid) return "invalid"
        return "%.3f".format(decimalValue)
    }
}
