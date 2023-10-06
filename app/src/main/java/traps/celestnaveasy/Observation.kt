package traps.celestnaveasy

data class Observation(

    val year: Int = 2022,
    val month: Int = 5,
    val day: Int = 1,
    val hour: Int = 12,
    val min: Int = 0,
    val sec: Double = 0.0,
    val latitude: Double = 40.689517,   // positive north
    val longitude: Double = -74.044866  // positive east

)
