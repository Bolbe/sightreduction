package traps.celestnaveasy

import traps.celestnaveasy.spa.SPAlgo
import traps.celestnaveasy.spa.SPData
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

object Calc {

    private val spa = SPAlgo()

    fun sexaToDecimal(deg: Int, minute: Double): Double {
        return if (deg>=0) deg+minute/60 else deg-minute/60
    }

    // returns a pair (int, double), first is deg, second is decimal minutes
    fun decimalToSexa(deg: Double): Pair<Int, Double> {
        return Pair(deg.toInt(), abs(deg-deg.toInt())*60)
    }

    fun decimalToSexaStr(deg:Double): String {
        val v = decimalToSexa(deg)
        return "${v.first}° %.1f′".format(v.second)
    }

    fun dayFractionToString(time: Double): String {
        val hour = time.toInt()
        val decimalMin = (time-hour)*60
        val min = decimalMin.toInt()
        val sec = ((decimalMin-min)*60).toInt()
        return "$hour:$min:$sec"
    }

    fun dayFraction(str: String): Double {
        return try {
            LocalTime.parse(str, DateTimeFormatter.ISO_LOCAL_TIME).toSecondOfDay() / 3600.0
        } catch (e: Exception) {
            -1.0
        }
    }

    // returns empty string if invalid or corrected string if valid.
    fun checkAndCorrectTimeFormat(str: String): String =

        // if colons found, check the number of characters is 8, if 7, add 0
        if (str.contains(':')) try {
                val localTime = LocalTime.parse(str, DateTimeFormatter.ISO_LOCAL_TIME)
                localTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
            } catch (e: Exception) {
                ""
            }

        else try {
                val localTime = LocalTime.parse(str, DateTimeFormatter.ofPattern("HHmmss"))
                localTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
            } catch (e: Exception) {
                ""
            }


    /*
        Return sun position from observer location
     */
    fun sunPositionFromObserver(obs: Observation): SunPosition {
        val data = SPData(obs.year, obs.month, obs.day, obs.hour, obs.min, obs.sec, obs.latitude, obs.longitude)
        val error = spa.calculate(data)
        return if (error!=0) {
            SunPosition(error)
        } else {
            SunPosition(0, data.delta_prime, data.sun_ha, data.h_prime, data.azimuth, data.e0)
        }
    }

    /*
        Returns the corrected elevation, given month, height of eye above sea, elevation
     */
    fun correctedElevation(elevation: Double, eyeHeight: Double, month: Int): Double {
        return elevation +
                CorrectionTableRefraction.correctionForElevation(elevation)/60 +
                CorrectionTableDip.correctionForHeight(eyeHeight)/60 +
                CorrectionTableDiameter.correctionForMonth(month)/60
    }

}

