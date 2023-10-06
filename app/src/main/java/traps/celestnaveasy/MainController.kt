package traps.celestnaveasy

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import traps.celestnaveasy.spa.SPAlgo
import traps.celestnaveasy.spa.SPData
import java.time.LocalDate
import kotlin.math.abs

class MainController(private val pref: SharedPreferences) {

    private val today: LocalDate = LocalDate.now()
    //private val today: LocalDate = LocalDate.parse("1998-03-04")
    var day = today.dayOfMonth
    val dayStr = mutableStateOf(day.toString())
    var month = today.monthValue
    val monthStr = mutableStateOf(month.toString())
    var year = today.year
    val yearStr = mutableStateOf(year.toString())

    val latitudeDMS = DMS("47", "29.0", true)
    val longitudeDMS = DMS("2", "53.0", false)
    var latG = 47.483
    var lonG = -2.883

    val correctionIndexStr = mutableStateOf("0")
    var correctionIndex = 0.0
    val eyeHeightStr = mutableStateOf("2.5")
    var eyeHeight = 2.5
    var correctionDip = 0.0
    var correctionRefraction = 0.0
    var correctionDiameter = 0.0
    var totalCorrection = 0.0
    var selectedMeasure: Measure? = null
    var correctedMeasure = -1.0
    var rawMeasure = -1.0
    var sunDeclination = 0.0
    var sunHourAngle = 0.0
    var localHourAngle = 0.0
    var azimuth = -1
    var azimuth180 = -1
    var elevation = 0.0
    var eot = 0.0
    var intercept = 0.0
    var towards_sun = true
    var explain = ""
    var error = mutableStateOf("")

    val dataReady = mutableStateOf(false)

    val fiveMeasures = mutableStateOf(false)
    val measureList = listOf(
        Measure("Mesure 1", "", "", "", "", ""),
        Measure("Mesure 2", "", "", "", "", ""),
        Measure("Mesure 3", "", "", "", "", ""),
        Measure("Mesure 4", "", "", "", "", ""),
        Measure("Mesure 5", "", "", "", "", ""),
//        Measure("Mesure 1", "15", "24", "04", "22", "59"),
//        Measure("Mesure 2", "15", "40", "0", "23", "00"),
//        Measure("Mesure 3", "15", "31", "0", "23", "01"),
//        Measure("Mesure 4", "15", "31", "55", "23", "02"),
//        Measure("Mesure 5", "15", "32", "45", "23", "03"),
    )

    init {
        eyeHeightStr.value = pref.getString("EyeHeight", "2.5").toString()
        correctionIndexStr.value = pref.getString("CorrectionIndex", "0.0").toString()
        latitudeDMS.degStr.value = pref.getString("LatGDeg", "47").toString()
        latitudeDMS.minStr.value = pref.getString("LatGMin", "").toString()
        latitudeDMS.positive.value = pref.getBoolean("LatGPositive", true)
        longitudeDMS.degStr.value = pref.getString("LonGDeg", "").toString()
        longitudeDMS.minStr.value = pref.getString("LonGMin", "").toString()
        longitudeDMS.positive.value = pref.getBoolean("LonGPositive", true)
    }
    private fun setError(str: String) {
        error.value = str
        dataReady.value = false
        selectedMeasure = null
    }

    private fun savePreferences() {
        with (pref.edit()) {
            putString("EyeHeight", eyeHeightStr.value)
            putString("CorrectionIndex", correctionIndexStr.value)
            putString("LatGDeg", latitudeDMS.degStr.value)
            putString("LatGMin", latitudeDMS.minStr.value)
            putBoolean("LatGPositive", latitudeDMS.positive.value)
            putString("LonGDeg", longitudeDMS.degStr.value)
            putString("LonGMin", longitudeDMS.minStr.value)
            putBoolean("LonGPositive", longitudeDMS.positive.value)
            commit()
        }

    }

    fun compute() {

        try {
            year = yearStr.value.toInt()
            month = monthStr.value.toInt()
            day = dayStr.value.toInt()
        } catch (e:Exception) {
            setError("Date du jour non valide")
            return
        }

        if (!latitudeDMS.validate()) {
            setError("Latitude G non valide")
            return
        }
        latG = latitudeDMS.decimalValue
        if (!longitudeDMS.validate()) {
            setError("Longitude G non valide")
            return
        }
        lonG = longitudeDMS.decimalValue

        selectedMeasure==null
        error.value = ""
        if (!fiveMeasures.value) {
            if (measureList[0].validate()) {
                selectedMeasure = measureList[0]
                dataReady.value = true
            }
            else {
                setError("Mesure vide ou invalide")
                return
            }
        }
        else {
            measureList.forEach {
                if (!it.validate()) {
                    setError("Mesure vide ou invalide")
                    return
                }
            }
            val i = indexBestMeasure()
            if (i<0) {
                setError("Impossible de choisir la meilleur mesure. N'utilisez qu'une seule mesure.")
                return
            }
            selectedMeasure = measureList[i]
            dataReady.value = true
        }
        if (selectedMeasure==null) return

        val data = SPData(
            year,
            month,
            day,
            selectedMeasure!!.timeValue.hour,
            selectedMeasure!!.timeValue.minute,
            selectedMeasure!!.timeValue.second.toDouble(),
            latG,
            lonG
        )
        val algo = SPAlgo()
        val errorAlgo = algo.calculate(data)
        if (errorAlgo!=0) {
            setError("Erreur dans le calcul de la position du soleil.")
            return
        }

        sunDeclination = data.delta_prime

        sunHourAngle = data.sun_ha
        println("GHA=${sunHourAngle}")
        localHourAngle = data.h_prime
        println("LHA=${localHourAngle}")
        azimuth = data.azimuth.toInt()
        azimuth180 = azimuth - 180
        if (azimuth180<0) azimuth180+=360
        elevation = data.e0
        eot = data.eot

        rawMeasure = Calc.sexaToDecimal(selectedMeasure!!.degValue, selectedMeasure!!.minValue)
        correctionIndex = correctionIndexStr.value.toDouble()
        eyeHeight = eyeHeightStr.value.toDouble()
        correctionDip = CorrectionTableDip.correctionForHeight(eyeHeight)
        correctionRefraction = CorrectionTableRefraction.correctionForElevation(data.e0)
        correctionDiameter = CorrectionTableDiameter.correctionForMonth(month)

        totalCorrection = correctionIndex + correctionDip + correctionRefraction + correctionDiameter
        correctedMeasure = rawMeasure + totalCorrection / 60

        towards_sun = correctedMeasure>=data.e0
        intercept = abs(data.e0-correctedMeasure)*60

        savePreferences()
    }

    private fun indexBestMeasure(): Int {

        val list = mutableListOf<Pair<Double, Double>>()
        measureList.forEach {
            list.add(Pair(it.dayFrac, Calc.sexaToDecimal(it.degValue, it.minValue)))
        }
        val dataSet = DataSet(list)

        explain = "best point: ${dataSet.bestPoint}\n" +
                "best point distance:${dataSet.bestDistance}\n" +
                "3 point group: ${dataSet.bestPointGroup}\n" +
                "Index of best point in group: ${dataSet.bestPointInGroup}\n" +
                "distance list: ${dataSet.distanceList()}\n" +
                "sse list: ${dataSet.sseList()}"

        if (dataSet.bestPoint==null) return -1

        val bestPointIndex = dataSet.bestPointGroup[dataSet.bestPointInGroup]

        return bestPointIndex
    }


}