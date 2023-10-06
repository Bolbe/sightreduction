package traps.celestnaveasy

import androidx.compose.runtime.mutableStateOf

class HMS {

    val hourStr = mutableStateOf("")
    val minStr = mutableStateOf("")
    val secStr = mutableStateOf("")
    var hour = 0
    var min = 0
    var sec = 0

    private var valid = false

    fun validate(): Boolean {
        valid = false
        try {
            hour = hourStr.value.toInt()
            min = minStr.value.toInt()
            sec = secStr.value.toInt()
        } catch (e: Exception) {
            return false
        }
        if (hour<0 || hour>23 || min<0 || min>59 || sec<0 || sec>59) return false
        valid = true
        hourStr.value = hour.toString()
        minStr.value = min.toString()
        secStr.value = sec.toString()
        return true
    }

    fun isValid() = valid

}

