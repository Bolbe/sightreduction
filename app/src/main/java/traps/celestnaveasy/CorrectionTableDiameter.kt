package traps.celestnaveasy

object CorrectionTableDiameter {

    // half diameter of the sun for each month
    private val lowerLimb = doubleArrayOf(16.3, 16.2, 16.1, 15.9, 15.8, 15.7, 15.7, 15.8, 15.9, 16.0, 16.2, 16.3)

    fun correctionForMonth(month: Int) : Double {
        var index = month-1
        if (index>11) index = 11
        if (index<0) index = 0
        return lowerLimb[index]
    }


}