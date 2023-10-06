package traps.celestnaveasy

import androidx.compose.runtime.mutableStateOf
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Measure (
    val title: String,
    timeHourParam: String = "",
    timeMinuteParam: String = "",
    timeSecondParam: String = "",
    degParam: String = "",
    minParam: String = ""
) {

    val timeHour = mutableStateOf(timeHourParam)
    val timeMinute = mutableStateOf(timeMinuteParam)
    val timeSecond = mutableStateOf(timeSecondParam)
    val deg = mutableStateOf(degParam)
    val min = mutableStateOf(minParam)

    var degValue = -1
    var minValue = -1.0
    var timeValue = LocalTime.now()
    var dayFrac = 0.0

    // return empty string if OK, error message if not.
    fun validate(): Boolean {
        try {
            timeValue = LocalTime.of(
                timeHour.value.toInt(),
                timeMinute.value.toInt(),
                timeSecond.value.toInt()
            )
            dayFrac = timeValue.toSecondOfDay() / 3600.0

            degValue = this.deg.value.toInt()
            minValue = this.min.value.toDouble()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun clear() {
        this.timeHour.value = ""
        this.timeMinute.value = ""
        this.timeSecond.value = ""
        this.deg.value = ""
        this.min.value = ""
    }

}