package traps.celestnaveasy

data class SunPosition(

    val error: Int = -1,    // not initialized
    val declination : Double = 0.0,
    val GHA : Double = 0.0,
    val LHA : Double = 0.0,
    val Azimuth: Double = 0.0,
    val Hc: Double = 0.0

) {
    constructor(error: Int) :
            this(error, 0.0, 0.0,0.0,0.0,0.0)

    constructor(declination: Double, GHA: Double, LHA: Double, Azimuth: Double, Hc: Double):
            this(0, declination, GHA, LHA, Azimuth, Hc)

}
