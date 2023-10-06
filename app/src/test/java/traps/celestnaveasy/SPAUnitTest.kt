package traps.celestnaveasy

import org.junit.Test

import org.junit.Assert.*
import traps.celestnaveasy.Calc.decimalToSexa
import traps.celestnaveasy.Calc.decimalToSexaStr
import traps.celestnaveasy.spa.SPAlgo
import traps.celestnaveasy.spa.SPData
import kotlin.math.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SPAUnitTest {
    @Test
    fun test1() {
        println("Test 1")
        val latG = Calc.sexaToDecimal(47, 29.0)
        println("latG : ${latG}")
        val lonG = Calc.sexaToDecimal(-2, 53.0)
        println("lonG : ${lonG}")
        val data = SPData(1998, 3, 4, 15, 24, 4.0, latG, lonG)
        val algo = SPAlgo()
        val error = algo.calculate(data)
        
        println(data)

        println("Sun declination : ${data.delta_prime} | ${decimalToSexaStr(data.delta_prime)}")
        println("Sun Hour Angle  : ${data.sun_ha} | ${decimalToSexaStr(data.sun_ha)}")
        println("Local Hour Angle: ${data.h_prime} | ${decimalToSexaStr(data.h_prime)}")
        println("Azimuth         : ${data.azimuth} | ${decimalToSexaStr(data.azimuth)}")

        val e = decimalToSexa(data.e0)
        println("Elevation       : ${data.e0} (${e.first} deg ${e.second} min)")

        val eot = decimalToSexa(data.eot)
        println("Equation of time: ${data.eot} (${eot.first} min ${eot.second} s)")

        // The measured angle in a Pair<Int, double> (deg, min)
        val measured = Pair<Int, Double>(22, 59.0)
        val measuredDecimal = Calc.sexaToDecimal(measured.first, measured.second)
        println("Measured angle with sextant: $measured | $measuredDecimal")

        // Correction instrument

        val correctionDip = CorrectionTableDip.correctionForHeight(2.5)
        println("Dip correction (eye height 2.5): $correctionDip")

        val correctionRefraction = CorrectionTableRefraction.correctionForElevation(data.e0)
        println("Correction refraction with elevation: $correctionRefraction")

        val correctionDiameter = CorrectionTableDiameter.correctionForMonth(3)
        println("Correction hal diameter with month: $correctionDiameter")

        val totalCorrection = correctionDip + correctionRefraction + correctionDiameter
        println("Total correction in minutes: $totalCorrection")

        val corrected = measuredDecimal + totalCorrection / 60
        println("Corrected measured elevation: $corrected | ${decimalToSexa(corrected)}")

        if (corrected>data.e0) {
            println("The intercept goes TOWARDS the sun subpoint.")
        } else {
            println("The intercept goes AWAY FROM the sun subpoint.")
        }

        val intercept = abs(data.e0-corrected)*60
        println("The intercept measures (in NM): $intercept")

    }
}