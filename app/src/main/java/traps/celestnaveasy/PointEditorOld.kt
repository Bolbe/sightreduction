package traps.celestnaveasy

import android.graphics.Color
import android.widget.EditText
import android.widget.ImageView
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class PointEditorOld(
//    val checkIcon: ImageView,
//    val timeEditor: EditText,
//    val degEditor: EditText,
//    val minEditor: EditText,
//    clearButton: AppCompatImageButton
) {
//
//    interface ChangeValueListener {
//        fun onValueChanged()
//    }
//
//    private var changeValueListener: ChangeValueListener? = null
//
//    var timeValue = -1.0 // decimal hour of day
//        private set
//
//    var degValue = -1
//        private set
//
//    var minValue = -1.0
//        private set
//
//    init {
//        timeEditor.setOnFocusChangeListener { _, b ->
//            if (b) return@setOnFocusChangeListener
//            setTimeValueFromEditor()
//
//            changeValueListener?.onValueChanged()
//        }
//
//        degEditor.setOnFocusChangeListener { _, b ->
//            if (b) return@setOnFocusChangeListener
//            setDegValueFromEditor()
//        }
//
//        minEditor.setOnFocusChangeListener { _, b ->
//            if (b) return@setOnFocusChangeListener
//            setMinValueFromEditor()
//        }
//
//        clearButton.setOnClickListener {
//            clear()
//        }
//
//    }
//
//    fun validate(): Boolean {
//        setTimeValueFromEditor()
//        setDegValueFromEditor()
//        setMinValueFromEditor()
//        return (timeValue>=0 && degValue>=0 && minValue>=0)
//    }
//
//    fun elevationValue() = degValue+minValue/60.0
//    fun clear() {
//        timeEditor.setText("")
//        timeEditor.setTextColor(Color.BLACK)
//        timeEditor.setBackgroundColor(Color.WHITE)
//        timeValue = -1.0
//        degEditor.setText("")
//        degEditor.setTextColor(Color.BLACK)
//        degEditor.setBackgroundColor(Color.WHITE)
//        degValue = -1
//        minEditor.setText("")
//        minEditor.setTextColor(Color.BLACK)
//        minEditor.setBackgroundColor(Color.WHITE)
//        minValue = -1.0
//    }
//
//    fun setChangeValueListener(listener: ChangeValueListener) {
//        changeValueListener = listener
//    }
//
//    fun resetHighlight() {
//        degEditor.setBackgroundColor(Color.WHITE)
//        minEditor.setBackgroundColor(Color.WHITE)
//        timeEditor.setBackgroundColor(Color.WHITE)
//    }
//
//    fun setHighlightSelected() {
//        degEditor.setBackgroundColor(LIGHT_GREEN)
//        minEditor.setBackgroundColor(LIGHT_GREEN)
//        timeEditor.setBackgroundColor(LIGHT_GREEN)
//    }
//
//    fun setHighlightBest() {
//        degEditor.setBackgroundColor(Color.GREEN)
//        minEditor.setBackgroundColor(Color.GREEN)
//        timeEditor.setBackgroundColor(Color.GREEN)
//    }
//
//    private fun setDegValueFromEditor() {
//        degValue = -1
//        if (degEditor.text.isBlank()) {
//            degEditor.setTextColor(Color.BLACK)
//            degEditor.setBackgroundColor(Color.WHITE)
//            return
//        }
//        val deg = degEditor.text.toString().toInt()
//        if (deg in 0..90) degValue = deg
//        if (degValue<0) {
//            degEditor.setTextColor(Color.WHITE)
//            degEditor.setBackgroundColor(Color.RED)
//        } else {
//            degEditor.setTextColor(Color.BLACK)
//            degEditor.setBackgroundColor(Color.WHITE)
//        }
//    }
//
//    private fun setMinValueFromEditor() {
//        minValue = -1.0
//        if (minEditor.text.isBlank())  {
//            minEditor.setTextColor(Color.BLACK)
//            minEditor.setBackgroundColor(Color.WHITE)
//            return
//        }
//        val min = minEditor.text.toString().toDouble()
//        if (min>=0 && min<60) minValue = min
//
//        if (minValue<0) {
//            minEditor.setTextColor(Color.WHITE)
//            minEditor.setBackgroundColor(Color.RED)
//        } else {
//            minEditor.setTextColor(Color.BLACK)
//            minEditor.setBackgroundColor(Color.WHITE)
//        }
//    }
//
//    private fun setTimeValueFromEditor() {
//        timeValue = -1.0
//        if (timeEditor.text.isBlank()) {
//            timeEditor.setTextColor(Color.BLACK)
//            timeEditor.setBackgroundColor(Color.WHITE)
//            return
//        }
//        val timeValueOK = checkAndCorrectTimeFormat(timeEditor)
//        if (timeValueOK) {
//            timeValue = dayFraction()
//        }
//        if (timeValue<0) {
//            timeEditor.setTextColor(Color.WHITE)
//            timeEditor.setBackgroundColor(Color.RED)
//        }
//        else {
//            timeEditor.setTextColor(Color.BLACK)
//            timeEditor.setBackgroundColor(Color.WHITE)
//        }
//    }
//
//    private fun dayFraction(): Double {
//        return try {
//            LocalTime.parse(timeEditor.text, DateTimeFormatter.ISO_LOCAL_TIME).toSecondOfDay() / 3600.0
//        } catch (e: Exception) {
//            -1.0
//        }
//    }
//    // return the decimal number of hours, -1.0 if error
//    private fun checkAndCorrectTimeFormat(editText: EditText): Boolean {
//
//        // if colons found, check the number of characters is 8, if 7, add 0
//        val str = editText.text.toString()
//        if (str.contains(':')) {
//            try {
//                val localTime = LocalTime.parse(str, DateTimeFormatter.ISO_LOCAL_TIME)
//                editText.setText(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
//            } catch (e: Exception) {
//                return false
//            }
//        }
//        else {
//            try {
//                val localTime = LocalTime.parse(str, DateTimeFormatter.ofPattern("HHmmss"))
//                editText.setText(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
//            } catch (e: Exception) {
//                return false
//            }
//        }
//        return true
//    }
//
//    companion object {
//
//        private val LIGHT_GREEN = Color.rgb(200,255,200)
//        private val YELLOW = Color.rgb(255,255,200)
//
//    }
}