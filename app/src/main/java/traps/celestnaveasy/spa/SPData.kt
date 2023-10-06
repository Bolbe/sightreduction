package traps.celestnaveasy.spa

/**
 * Data structure for Solar Position algorithm
 */
data class SPData (

    // Input data
    // ============================================================================================

    val year: Int = 2022,          // 4-digit year,      valid range: -2000 to 6000, error code: 1
    val month: Int = 5,         // 2-digit month,         valid range: 1 to  12,  error code: 2
    val day: Int = 1,           // 2-digit day,           valid range: 1 to  31,  error code: 3
    var hour: Int = 15,          // Observer local hour,   valid range: 0 to  24,  error code: 4
    var minute: Int = 0,        // Observer local minute, valid range: 0 to  59,  error code: 5
    var second: Double = 0.0,     // Observer local second, valid range: 0 to <60,  error code: 6

    val latitude: Double = 40.689517,   // Observer latitude (negative south of equator)
    // valid range: -90   to   90 degrees, error code: 10

    val longitude: Double = -74.044866,  // Observer longitude (negative west of Greenwich)
    // valid range: -180  to  180 degrees, error code: 9

//    val delta_ut1: Double = 0.0,  // Fractional second difference between UTC and UT which is used
//                            // to adjust UTC for earth's irregular rotation rate and is derived
//                            // from observation only and is reported in this bulletin:
//                            // http://maia.usno.navy.mil/ser7/ser7.dat,
//                            // where delta_ut1 = DUT1
//                            // valid range: -1 to 1 second (exclusive), error code 17

    // Output data
    // ===================================================================================================
    var jd: Double = 0.0,        //Julian day
    var jc: Double = 0.0,         //Julian century

    var jde: Double = 0.0,         //Julian ephemeris day
    var jce: Double = 0.0,         //Julian ephemeris century
    var jme: Double = 0.0,         //Julian ephemeris millennium

    var l: Double = 0.0,           //earth heliocentric longitude [degrees]
    var b: Double = 0.0,           //earth heliocentric latitude [degrees]
    var r: Double = 0.0,           //earth radius vector [Astronomical Units, AU]

    var theta: Double = 0.0,       //geocentric longitude [degrees]
    var beta: Double = 0.0,        //geocentric latitude [degrees]

    var x0: Double = 0.0,          //mean elongation (moon-sun) [degrees]
    var x1: Double = 0.0,          //mean anomaly (sun) [degrees]
    var x2: Double = 0.0,          //mean anomaly (moon) [degrees]
    var x3: Double = 0.0,          //argument latitude (moon) [degrees]
    var x4: Double = 0.0,          //ascending longitude (moon) [degrees]

    var del_psi: Double = 0.0,     //nutation longitude [degrees]
    var del_epsilon: Double = 0.0, //nutation obliquity [degrees]
    var epsilon0: Double = 0.0,    //ecliptic mean obliquity [arc seconds]
    var epsilon: Double = 0.0,     //ecliptic true obliquity  [degrees]

    var del_tau: Double = 0.0,     //aberration correction [degrees]
    var lamda: Double = 0.0,       //apparent sun longitude [degrees]
    var nu0: Double = 0.0,         //Greenwich mean sidereal time [degrees]
    var nu: Double = 0.0,          //Greenwich sidereal time [degrees]

    var alpha: Double = 0.0,       //geocentric sun right ascension [degrees]
    var delta: Double = 0.0,       //geocentric sun declination [degrees]

    var h: Double = 0.0,           //observer hour angle [degrees]
    var xi: Double = 0.0,          //sun equatorial horizontal parallax [degrees]
    var del_alpha: Double = 0.0,   //sun right ascension parallax [degrees]
    var delta_prime: Double = 0.0, //topocentric sun declination [degrees]
    var alpha_prime: Double = 0.0, //topocentric sun right ascension [degrees]
    var h_prime: Double = 0.0,     //topocentric local hour angle [degrees]

    var e0: Double = 0.0,          //topocentric elevation angle (uncorrected) [degrees]

    var eot: Double = 0.0,         //equation of time [minutes]
    var azimuth_astro: Double = 0.0,//topocentric azimuth angle (westward from south) [for astronomers]
    var azimuth: Double = 0.0,      //topocentric azimuth angle (eastward from north) [for navigators and solar radiation]

    var sun_ha: Double = 0.0        // sun hour angle (spa.longitude - spa.h_prime)

)