package traps.celestnaveasy.spa

import kotlin.math.*

/**
 *  https://midcdmz.nrel.gov/spa/
 */
class SPAlgo() {

    private lateinit var spdata: SPData

    private fun limit_degrees(degrees: Double): Double
    {
        val degrees2 = degrees / 360.0
        var limited: Double = 360.0*(degrees2-floor(degrees2))
        if (limited < 0) limited += 360.0
        return limited
    }

    private fun limit_degrees180pm(degrees: Double): Double
    {
        val degrees2 = degrees / 360.0
        var limited: Double = 360.0*(degrees2-floor(degrees2))
        if (limited < -180.0) limited += 360.0
        else if (limited >  180.0) limited -= 360.0
        return limited
    }

    private fun limit_minutes(minutes: Double): Double
    {
        var limited=minutes

        if      (limited < -20.0) limited += 1440.0
        else if (limited >  20.0) limited -= 1440.0
        return limited
    }

    private fun third_order_polynomial(a: Double, b: Double, c: Double, d: Double, x: Double): Double  {
        return ((a*x + b)*x + c)*x + d
    }

///////////////////////////////////////////////////////////////////////////////////////////////
    private fun validate_inputs(spa: SPData): Int {
        if ((spa.year        < -2000) || (spa.year        > 6000)) return 1
        if ((spa.month       < 1    ) || (spa.month       > 12  )) return 2
        if ((spa.day         < 1    ) || (spa.day         > 31  )) return 3
        if ((spa.hour        < 0    ) || (spa.hour        > 24  )) return 4
        if ((spa.minute      < 0    ) || (spa.minute      > 59  )) return 5
        if ((spa.second      < 0    ) || (spa.second      >=60  )) return 6
        if ((spa.hour        == 24  ) && (spa.minute      > 0   )) return 5
        if ((spa.hour        == 24  ) && (spa.second      > 0   )) return 6

        if (spa.longitude.absoluteValue     > 180     ) return 9
        if (spa.latitude.absoluteValue      > 90      ) return 10

        return 0
    }
///////////////////////////////////////////////////////////////////////////////////////////////
    private fun julian_day (year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Double): Double
    {

        val day_decimal = day + (hour + (minute + second/60.0)/60.0)/24.0

        var month2 = month
        var year2 = year
        if (month < 3) {
            month2 += 12
            year2--
        }

        var julian_day = (365.25*(year2+4716.0)).toInt() + (30.6001*(month2+1)).toInt() + day_decimal - 1524.5

        if (julian_day > 2299160.0) {
            val a = (year2/100).toInt()
            julian_day += (2 - a + (a/4).toInt())
        }

        return julian_day
    }

    private fun julian_century(jd: Double): Double {
        return (jd-2451545.0)/36525.0
    }

    private fun julian_ephemeris_day(jd: Double, delta_t: Double): Double {
        return jd+delta_t/86400.0
    }

    private fun julian_ephemeris_century(jde: Double): Double {
        return (jde - 2451545.0)/36525.0
    }

    private fun julian_ephemeris_millennium(jce: Double): Double {
        return (jce/10.0)
    }

    private fun earth_periodic_term_summation(terms: Array<DoubleArray>, count: Int, jme: Double): Double {

        var sum=0.0

        for (i in 0 until count)
            sum += terms[i][TERM_A]*cos(terms[i][TERM_B]+terms[i][TERM_C]*jme)

        return sum
    }

    private fun earth_values(term_sum: DoubleArray, count: Int, jme: Double): Double {

        var sum=0.0

        for (i in 0 until count)
            sum += term_sum[i]* jme.pow(i)

        sum /= 1.0e8

        return sum
    }

    private fun earth_heliocentric_longitude(jme: Double): Double {
        val sum = DoubleArray(L_COUNT)

        for (i in 0 until L_COUNT)
            sum[i] = earth_periodic_term_summation(L_TERMS[i], l_subcount[i], jme)

        return limit_degrees(Math.toDegrees(earth_values(sum, L_COUNT, jme)))

    }

    private fun earth_heliocentric_latitude(jme: Double): Double {
        val sum = DoubleArray(B_COUNT)

        for (i in 0 until B_COUNT)
            sum[i] = earth_periodic_term_summation(B_TERMS[i], b_subcount[i], jme)

        return Math.toDegrees(earth_values(sum, B_COUNT, jme))

    }

    private fun earth_radius_vector(jme: Double): Double {
        val sum = DoubleArray(R_COUNT)

        for (i in 0 until R_COUNT)
            sum[i] = earth_periodic_term_summation(R_TERMS[i], r_subcount[i], jme)

        return earth_values(sum, R_COUNT, jme)

    }

    private fun geocentric_longitude(l: Double): Double {
        var theta = l + 180.0

        if (theta >= 360.0) theta -= 360.0

        return theta
    }

    private fun geocentric_latitude(b: Double): Double {
        return -b
    }

    private fun mean_elongation_moon_sun(jce: Double): Double {
        return third_order_polynomial(1.0/189474.0, -0.0019142, 445267.11148, 297.85036, jce)
    }

    private fun mean_anomaly_sun(jce: Double): Double {
        return third_order_polynomial(-1.0/300000.0, -0.0001603, 35999.05034, 357.52772, jce)
    }

    private fun mean_anomaly_moon(jce: Double): Double {
        return third_order_polynomial(1.0/56250.0, 0.0086972, 477198.867398, 134.96298, jce)
    }

    private fun argument_latitude_moon(jce: Double): Double {
        return third_order_polynomial(1.0/327270.0, -0.0036825, 483202.017538, 93.27191, jce)
    }

    private fun ascending_longitude_moon(jce: Double): Double {
        return third_order_polynomial(1.0/450000.0, 0.0020708, -1934.136261, 125.04452, jce)
    }

    private fun xy_term_summation(i: Int, x: DoubleArray): Double {

        var sum = 0.0

        for (j in 0 until TERM_Y_COUNT)
            sum += x[j]*Y_TERMS[i][j]

        return sum
    }

    private fun nutation_longitude_and_obliquity(jce: Double, x: DoubleArray) : Pair<Double, Double> {

        var xy_term_sum: Double
        var sum_psi = 0.0
        var sum_epsilon = 0.0

        for (i in 0 until Y_COUNT) {
            xy_term_sum  = Math.toRadians(xy_term_summation(i, x))
            sum_psi     += (PE_TERMS[i][TERM_PSI_A] + jce*PE_TERMS[i][TERM_PSI_B])*sin(xy_term_sum)
            sum_epsilon += (PE_TERMS[i][TERM_EPS_C] + jce*PE_TERMS[i][TERM_EPS_D])*cos(xy_term_sum)
        }

        return Pair(sum_psi / 36000000.0, sum_epsilon / 36000000.0)

    }

    private fun ecliptic_mean_obliquity(jme: Double): Double  {
        val u = jme/10.0

        return 84381.448 + u*(-4680.93 + u*(-1.55 + u*(1999.25 + u*(-51.38 + u*(-249.67 +
                u*(  -39.05 + u*( 7.12 + u*(  27.87 + u*(  5.79 + u*2.45)))))))))
    }

    private fun ecliptic_true_obliquity(delta_epsilon: Double, epsilon0: Double): Double {
        return delta_epsilon + epsilon0/3600.0
    }

    private fun aberration_correction(r: Double): Double {
        return -20.4898 / (3600.0*r)
    }

    private fun apparent_sun_longitude(theta: Double, delta_psi: Double, delta_tau: Double): Double {
        return theta + delta_psi + delta_tau
    }

    private fun greenwich_mean_sidereal_time (jd: Double, jc: Double): Double {
        return limit_degrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                jc*jc*(0.000387933 - jc/38710000.0))
    }

    private fun greenwich_sidereal_time (nu0: Double, delta_psi: Double, epsilon: Double): Double {
        return nu0 + delta_psi*cos(Math.toRadians(epsilon))
    }

    private fun geocentric_right_ascension(lamda: Double, epsilon: Double, beta: Double): Double {
        val lamda_rad   = Math.toRadians(lamda)
        val epsilon_rad = Math.toRadians(epsilon)

        return limit_degrees(Math.toDegrees(atan2(sin(lamda_rad)*cos(epsilon_rad) -
                tan(Math.toRadians(beta))*sin(epsilon_rad), cos(lamda_rad))))
    }

    private fun geocentric_declination(beta: Double, epsilon: Double, lamda: Double): Double {
        val beta_rad    = Math.toRadians(beta)
        val epsilon_rad = Math.toRadians(epsilon)

        return Math.toDegrees(asin(sin(beta_rad)*cos(epsilon_rad) +
                cos(beta_rad)*sin(epsilon_rad)*sin(Math.toRadians(lamda))))
    }

    private fun observer_hour_angle(nu: Double, longitude: Double, alpha_deg: Double): Double {
        return limit_degrees(nu + longitude - alpha_deg)
    }

    private fun sun_equatorial_horizontal_parallax(r: Double): Double {
        return 8.794 / (3600.0 * r)
    }

    private fun right_ascension_parallax_and_topocentric_dec(latitude: Double,
                                                             xi: Double,
                                                             h: Double,
                                                             delta: Double) : Pair<Double, Double>
    {
        var delta_alpha_rad: Double
        val lat_rad   = Math.toRadians(latitude)
        val xi_rad    = Math.toRadians(xi)
        val h_rad     = Math.toRadians(h)
        val delta_rad = Math.toRadians(delta)
        val u = atan(0.99664719 * tan(lat_rad))
        val y = 0.99664719 * sin(u) //+ elevation*sin(lat_rad)/6378140.0
        val x =              cos(u) //+ elevation*cos(lat_rad)/6378140.0

        delta_alpha_rad =      atan2(                - x*sin(xi_rad) *sin(h_rad),
            cos(delta_rad) - x*sin(xi_rad) *cos(h_rad))

        val delta_prime2 = Math.toDegrees(atan2((sin(delta_rad) - y*sin(xi_rad))*cos(delta_alpha_rad),
        cos(delta_rad) - x*sin(xi_rad) *cos(h_rad)))

        val delta_alpha2 = Math.toDegrees(delta_alpha_rad)

        return Pair(delta_alpha2, delta_prime2)
    }

    private fun topocentric_right_ascension(alpha_deg: Double, delta_alpha: Double): Double
    {
        return alpha_deg + delta_alpha
    }

    private fun topocentric_local_hour_angle(h: Double, delta_alpha: Double): Double
    {
        return h - delta_alpha
    }

    private fun topocentric_elevation_angle(latitude: Double, delta_prime: Double, h_prime: Double): Double
    {
        val lat_rad         = Math.toRadians(latitude)
        val delta_prime_rad = Math.toRadians(delta_prime)

        return Math.toDegrees(asin(sin(lat_rad)*sin(delta_prime_rad) +
                cos(lat_rad)*cos(delta_prime_rad) * cos(Math.toRadians(h_prime))))
    }



    private fun topocentric_azimuth_angle_astro(h_prime: Double,
                                                latitude: Double,
                                                delta_prime: Double): Double
    {
        val h_prime_rad = Math.toRadians(h_prime)
        val lat_rad     = Math.toRadians(latitude)

        return limit_degrees(Math.toDegrees(atan2(sin(h_prime_rad),
            cos(h_prime_rad)*sin(lat_rad) - tan(Math.toRadians(delta_prime))*cos(lat_rad))))
    }

    private fun topocentric_azimuth_angle(azimuth_astro: Double): Double  {
        return limit_degrees(azimuth_astro + 180.0)
    }

    private fun sun_mean_longitude(jme: Double): Double  {
        return limit_degrees(280.4664567 + jme*(360007.6982779 + jme*(0.03032028 +
                jme*(1/49931.0   + jme*(-1/15300.0     + jme*(-1/2000000.0))))))
    }

    private fun eot(m: Double, alpha: Double, del_psi: Double, epsilon: Double): Double {
        return limit_minutes(4.0*(m - 0.0057183 - alpha + del_psi*cos(Math.toRadians(epsilon))))
    }


////////////////////////////////////////////////////////////////////////////////////////////////
// Calculate required SPA parameters to get the right ascension (alpha) and declination (delta)
// Note: JD must be already calculated and in structure
////////////////////////////////////////////////////////////////////////////////////////////////
    private fun calculate_geocentric_sun_right_ascension_and_declination() {
        val x = DoubleArray(TERM_X_COUNT)

        spdata.jc = julian_century(spdata.jd)

        spdata.jde = julian_ephemeris_day(spdata.jd, DELTA_T)
        spdata.jce = julian_ephemeris_century(spdata.jde)
        spdata.jme = julian_ephemeris_millennium(spdata.jce)

        spdata.l = earth_heliocentric_longitude(spdata.jme)
        spdata.b = earth_heliocentric_latitude(spdata.jme)
        spdata.r = earth_radius_vector(spdata.jme)

        spdata.theta = geocentric_longitude(spdata.l)
        spdata.beta  = geocentric_latitude(spdata.b)

        spdata.x0 = mean_elongation_moon_sun(spdata.jce)
        spdata.x1 = mean_anomaly_sun(spdata.jce)
        spdata.x2 = mean_anomaly_moon(spdata.jce)
        spdata.x3 = argument_latitude_moon(spdata.jce)
        spdata.x4 = ascending_longitude_moon(spdata.jce)

        x[TERM_X0] = spdata.x0
        x[TERM_X1] = spdata.x1
        x[TERM_X2] = spdata.x2
        x[TERM_X3] = spdata.x3
        x[TERM_X4] = spdata.x4

        val (del_psi, del_epsilon) = nutation_longitude_and_obliquity(spdata.jce, x)
        spdata.del_psi = del_psi
        spdata.del_epsilon = del_epsilon

        spdata.epsilon0 = ecliptic_mean_obliquity(spdata.jme)
        spdata.epsilon  = ecliptic_true_obliquity(spdata.del_epsilon, spdata.epsilon0)

        spdata.del_tau   = aberration_correction(spdata.r)
        spdata.lamda     = apparent_sun_longitude(spdata.theta, spdata.del_psi, spdata.del_tau)
        spdata.nu0       = greenwich_mean_sidereal_time (spdata.jd, spdata.jc)
        spdata.nu        = greenwich_sidereal_time (spdata.nu0, spdata.del_psi, spdata.epsilon)

        spdata.alpha = geocentric_right_ascension(spdata.lamda, spdata.epsilon, spdata.beta)
        spdata.delta = geocentric_declination(spdata.beta, spdata.epsilon, spdata.lamda)
    }

    /**
     * Main function
     */
    fun calculate(spdata: SPData): Int {

        this.spdata = spdata

        val result = validate_inputs(spdata)

        if (result == 0) {
            spdata.jd = julian_day (spdata.year, spdata.month,  spdata.day, spdata.hour,
                spdata.minute, spdata.second)

            calculate_geocentric_sun_right_ascension_and_declination()

            spdata.h  = observer_hour_angle(spdata.nu, spdata.longitude, spdata.alpha)
            spdata.xi = sun_equatorial_horizontal_parallax(spdata.r)

            val (del_alpha, delta_prime) = right_ascension_parallax_and_topocentric_dec(
                                                spdata.latitude, spdata.xi,
                                                spdata.h, spdata.delta)

            spdata.del_alpha = del_alpha
            spdata.delta_prime = delta_prime

            spdata.alpha_prime = topocentric_right_ascension(spdata.alpha, spdata.del_alpha)
            spdata.h_prime     = topocentric_local_hour_angle(spdata.h, spdata.del_alpha)
            spdata.sun_ha       = limit_degrees180pm(spdata.longitude-spdata.h_prime)

            spdata.e0      = topocentric_elevation_angle(spdata.latitude, spdata.delta_prime, spdata.h_prime)
            spdata.azimuth_astro = topocentric_azimuth_angle_astro(spdata.h_prime, spdata.latitude, spdata.delta_prime)
            spdata.azimuth       = topocentric_azimuth_angle(spdata.azimuth_astro)

            val m = sun_mean_longitude(spdata.jme)
            spdata.eot = eot(m, spdata.alpha, spdata.del_psi, spdata.epsilon)

        }

        return result
    }

    companion object {

        private const val DELTA_T: Double = 69.184  // wikipedia says 32.184 + 37.0 https://en.wikipedia.org/wiki/International_Atomic_Time

        private const val L_COUNT = 6
        private const val B_COUNT = 2
        private const val R_COUNT = 5
        private const val Y_COUNT = 63

        private const val TERM_A = 0
        private const val TERM_B = 1
        private const val TERM_C = 2

        private const val TERM_X0 = 0
        private const val TERM_X1 = 1
        private const val TERM_X2 = 2
        private const val TERM_X3 = 3
        private const val TERM_X4 = 4
        private const val TERM_X_COUNT = 5

        private const val TERM_PSI_A = 0
        private const val TERM_PSI_B = 1
        private const val TERM_EPS_C = 2
        private const val TERM_EPS_D = 3

        private const val TERM_Y_COUNT = TERM_X_COUNT

        private val l_subcount = intArrayOf(64,34,20,7,3,1)
        private val b_subcount = intArrayOf(5,2)
        private val r_subcount = intArrayOf(40,10,6,2,1)

        private val L_TERMS = arrayOf(
            arrayOf(
                doubleArrayOf(175347046.0, 0.0, 0.0),
                doubleArrayOf(3341656.0,4.6692568,6283.07585),
                doubleArrayOf(34894.0,4.6261,12566.1517),
                doubleArrayOf(3497.0,2.7441,5753.3849),
                doubleArrayOf(3418.0,2.8289,3.5231),
                doubleArrayOf(3136.0,3.6277,77713.7715),
                doubleArrayOf(2676.0,4.4181,7860.4194),
                doubleArrayOf(2343.0,6.1352,3930.2097),
                doubleArrayOf(1324.0,0.7425,11506.7698),
                doubleArrayOf(1273.0,2.0371,529.691),
                doubleArrayOf(1199.0,1.1096,1577.3435),
                doubleArrayOf(990.0,5.233,5884.927),
                doubleArrayOf(902.0,2.045,26.298),
                doubleArrayOf(857.0,3.508,398.149),
                doubleArrayOf(780.0,1.179,5223.694),
                doubleArrayOf(753.0,2.533,5507.553),
                doubleArrayOf(505.0,4.583,18849.228),
                doubleArrayOf(492.0,4.205,775.523),
                doubleArrayOf(357.0,2.92,0.067),
                doubleArrayOf(317.0,5.849,11790.629),
                doubleArrayOf(284.0,1.899,796.298),
                doubleArrayOf(271.0,0.315,10977.079),
                doubleArrayOf(243.0,0.345,5486.778),
                doubleArrayOf(206.0,4.806,2544.314),
                doubleArrayOf(205.0,1.869,5573.143),
                doubleArrayOf(202.0,2.458,6069.777),
                doubleArrayOf(156.0,0.833,213.299),
                doubleArrayOf(132.0,3.411,2942.463),
                doubleArrayOf(126.0,1.083,20.775),
                doubleArrayOf(115.0,0.645,0.98),
                doubleArrayOf(103.0,0.636,4694.003),
                doubleArrayOf(102.0,0.976,15720.839),
                doubleArrayOf(102.0,4.267,7.114),
                doubleArrayOf(99.0,6.21,2146.17),
                doubleArrayOf(98.0,0.68,155.42),
                doubleArrayOf(86.0,5.98,161000.69),
                doubleArrayOf(85.0,1.3,6275.96),
                doubleArrayOf(85.0,3.67,71430.7),
                doubleArrayOf(80.0,1.81,17260.15),
                doubleArrayOf(79.0,3.04,12036.46),
                doubleArrayOf(75.0,1.76,5088.63),
                doubleArrayOf(74.0,3.5,3154.69),
                doubleArrayOf(74.0,4.68,801.82),
                doubleArrayOf(70.0,0.83,9437.76),
                doubleArrayOf(62.0,3.98,8827.39),
                doubleArrayOf(61.0,1.82,7084.9),
                doubleArrayOf(57.0,2.78,6286.6),
                doubleArrayOf(56.0,4.39,14143.5),
                doubleArrayOf(56.0,3.47,6279.55),
                doubleArrayOf(52.0,0.19,12139.55),
                doubleArrayOf(52.0,1.33,1748.02),
                doubleArrayOf(51.0,0.28,5856.48),
                doubleArrayOf(49.0,0.49,1194.45),
                doubleArrayOf(41.0,5.37,8429.24),
                doubleArrayOf(41.0,2.4,19651.05),
                doubleArrayOf(39.0,6.17,10447.39),
                doubleArrayOf(37.0,6.04,10213.29),
                doubleArrayOf(37.0,2.57,1059.38),
                doubleArrayOf(36.0,1.71,2352.87),
                doubleArrayOf(36.0,1.78,6812.77),
                doubleArrayOf(33.0,0.59,17789.85),
                doubleArrayOf(30.0,0.44,83996.85),
                doubleArrayOf(30.0,2.74,1349.87),
                doubleArrayOf(25.0,3.16,4690.48)
            ),
            arrayOf(
                doubleArrayOf(628331966747.0,0.0,0.0),
                doubleArrayOf(206059.0,2.678235,6283.07585),
                doubleArrayOf(4303.0,2.6351,12566.1517),
                doubleArrayOf(425.0,1.59,3.523),
                doubleArrayOf(119.0,5.796,26.298),
                doubleArrayOf(109.0,2.966,1577.344),
                doubleArrayOf(93.0,2.59,18849.23),
                doubleArrayOf(72.0,1.14,529.69),
                doubleArrayOf(68.0,1.87,398.15),
                doubleArrayOf(67.0,4.41,5507.55),
                doubleArrayOf(59.0,2.89,5223.69),
                doubleArrayOf(56.0,2.17,155.42),
                doubleArrayOf(45.0,0.4,796.3),
                doubleArrayOf(36.0,0.47,775.52),
                doubleArrayOf(29.0,2.65,7.11),
                doubleArrayOf(21.0,5.34,0.98),
                doubleArrayOf(19.0,1.85,5486.78),
                doubleArrayOf(19.0,4.97,213.3),
                doubleArrayOf(17.0,2.99,6275.96),
                doubleArrayOf(16.0,0.03,2544.31),
                doubleArrayOf(16.0,1.43,2146.17),
                doubleArrayOf(15.0,1.21,10977.08),
                doubleArrayOf(12.0,2.83,1748.02),
                doubleArrayOf(12.0,3.26,5088.63),
                doubleArrayOf(12.0,5.27,1194.45),
                doubleArrayOf(12.0,2.08,4694.0),
                doubleArrayOf(11.0,0.77,553.57),
                doubleArrayOf(10.0,1.3,6286.6),
                doubleArrayOf(10.0,4.24,1349.87),
                doubleArrayOf(9.0,2.7,242.73),
                doubleArrayOf(9.0,5.64,951.72),
                doubleArrayOf(8.0,5.3,2352.87),
                doubleArrayOf(6.0,2.65,9437.76),
                doubleArrayOf(6.0,4.67,4690.48)
            ),
            arrayOf(
                doubleArrayOf(52919.0,0.0,0.0),
                doubleArrayOf(8720.0,1.0721,6283.0758),
                doubleArrayOf(309.0,0.867,12566.152),
                doubleArrayOf(27.0,0.05,3.52),
                doubleArrayOf(16.0,5.19,26.3),
                doubleArrayOf(16.0,3.68,155.42),
                doubleArrayOf(10.0,0.76,18849.23),
                doubleArrayOf(9.0,2.06,77713.77),
                doubleArrayOf(7.0,0.83,775.52),
                doubleArrayOf(5.0,4.66,1577.34),
                doubleArrayOf(4.0,1.03,7.11),
                doubleArrayOf(4.0,3.44,5573.14),
                doubleArrayOf(3.0,5.14,796.3),
                doubleArrayOf(3.0,6.05,5507.55),
                doubleArrayOf(3.0,1.19,242.73),
                doubleArrayOf(3.0,6.12,529.69),
                doubleArrayOf(3.0,0.31,398.15),
                doubleArrayOf(3.0,2.28,553.57),
                doubleArrayOf(2.0,4.38,5223.69),
                doubleArrayOf(2.0,3.75,0.98)
            ),
            arrayOf(
                doubleArrayOf(289.0,5.844,6283.076),
                doubleArrayOf(35.0,0.0,0.0),
                doubleArrayOf(17.0,5.49,12566.15),
                doubleArrayOf(3.0,5.2,155.42),
                doubleArrayOf(1.0,4.72,3.52),
                doubleArrayOf(1.0,5.3,18849.23),
                doubleArrayOf(1.0,5.97,242.73)
            ),
            arrayOf(
                doubleArrayOf(114.0,3.142,0.0),
                doubleArrayOf(8.0,4.13,6283.08),
                doubleArrayOf(1.0,3.84,12566.15)
            ),
            arrayOf(
                doubleArrayOf(1.0, 3.14, 0.0)
            )
        )

        private val B_TERMS = arrayOf(
            arrayOf(
                doubleArrayOf(280.0,3.199,84334.662),
                doubleArrayOf(102.0,5.422,5507.553),
                doubleArrayOf(80.0,3.88,5223.69),
                doubleArrayOf(44.0,3.7,2352.87),
                doubleArrayOf(32.0,4.0,1577.34)
            ),
            arrayOf(
                doubleArrayOf(9.0,3.9,5507.55),
                doubleArrayOf(6.0,1.73,5223.69)
            )
        )

        private val R_TERMS = arrayOf(
            arrayOf(
                doubleArrayOf(100013989.0,0.0,0.0),
                doubleArrayOf(1670700.0,3.0984635,6283.07585),
                doubleArrayOf(13956.0,3.05525,12566.1517),
                doubleArrayOf(3084.0,5.1985,77713.7715),
                doubleArrayOf(1628.0,1.1739,5753.3849),
                doubleArrayOf(1576.0,2.8469,7860.4194),
                doubleArrayOf(925.0,5.453,11506.77),
                doubleArrayOf(542.0,4.564,3930.21),
                doubleArrayOf(472.0,3.661,5884.927),
                doubleArrayOf(346.0,0.964,5507.553),
                doubleArrayOf(329.0,5.9,5223.694),
                doubleArrayOf(307.0,0.299,5573.143),
                doubleArrayOf(243.0,4.273,11790.629),
                doubleArrayOf(212.0,5.847,1577.344),
                doubleArrayOf(186.0,5.022,10977.079),
                doubleArrayOf(175.0,3.012,18849.228),
                doubleArrayOf(110.0,5.055,5486.778),
                doubleArrayOf(98.0,0.89,6069.78),
                doubleArrayOf(86.0,5.69,15720.84),
                doubleArrayOf(86.0,1.27,161000.69),
                doubleArrayOf(65.0,0.27,17260.15),
                doubleArrayOf(63.0,0.92,529.69),
                doubleArrayOf(57.0,2.01,83996.85),
                doubleArrayOf(56.0,5.24,71430.7),
                doubleArrayOf(49.0,3.25,2544.31),
                doubleArrayOf(47.0,2.58,775.52),
                doubleArrayOf(45.0,5.54,9437.76),
                doubleArrayOf(43.0,6.01,6275.96),
                doubleArrayOf(39.0,5.36,4694.0),
                doubleArrayOf(38.0,2.39,8827.39),
                doubleArrayOf(37.0,0.83,19651.05),
                doubleArrayOf(37.0,4.9,12139.55),
                doubleArrayOf(36.0,1.67,12036.46),
                doubleArrayOf(35.0,1.84,2942.46),
                doubleArrayOf(33.0,0.24,7084.9),
                doubleArrayOf(32.0,0.18,5088.63),
                doubleArrayOf(32.0,1.78,398.15),
                doubleArrayOf(28.0,1.21,6286.6),
                doubleArrayOf(28.0,1.9,6279.55),
                doubleArrayOf(26.0,4.59,10447.39)
            ),
            arrayOf(
                doubleArrayOf(103019.0,1.10749,6283.07585),
                doubleArrayOf(1721.0,1.0644,12566.1517),
                doubleArrayOf(702.0,3.142,0.0),
                doubleArrayOf(32.0,1.02,18849.23),
                doubleArrayOf(31.0,2.84,5507.55),
                doubleArrayOf(25.0,1.32,5223.69),
                doubleArrayOf(18.0,1.42,1577.34),
                doubleArrayOf(10.0,5.91,10977.08),
                doubleArrayOf(9.0,1.42,6275.96),
                doubleArrayOf(9.0,0.27,5486.78)
            ),
            arrayOf(
                doubleArrayOf(4359.0,5.7846,6283.0758),
                doubleArrayOf(124.0,5.579,12566.152),
                doubleArrayOf(12.0,3.14,0.0),
                doubleArrayOf(9.0,3.63,77713.77),
                doubleArrayOf(6.0,1.87,5573.14),
                doubleArrayOf(3.0,5.47,18849.23)
            ),
            arrayOf(
                doubleArrayOf(145.0,4.273,6283.076),
                doubleArrayOf(7.0,3.92,12566.15)
            ),
            arrayOf(
                doubleArrayOf(4.0,2.56,6283.08)
            )
        )

        private val Y_TERMS = arrayOf(
            intArrayOf(0,0,0,0,1),
            intArrayOf(-2,0,0,2,2),
            intArrayOf(0,0,0,2,2),
            intArrayOf(0,0,0,0,2),
            intArrayOf(0,1,0,0,0),
            intArrayOf(0,0,1,0,0),
            intArrayOf(-2,1,0,2,2),
            intArrayOf(0,0,0,2,1),
            intArrayOf(0,0,1,2,2),
            intArrayOf(-2,-1,0,2,2),
            intArrayOf(-2,0,1,0,0),
            intArrayOf(-2,0,0,2,1),
            intArrayOf(0,0,-1,2,2),
            intArrayOf(2,0,0,0,0),
            intArrayOf(0,0,1,0,1),
            intArrayOf(2,0,-1,2,2),
            intArrayOf(0,0,-1,0,1),
            intArrayOf(0,0,1,2,1),
            intArrayOf(-2,0,2,0,0),
            intArrayOf(0,0,-2,2,1),
            intArrayOf(2,0,0,2,2),
            intArrayOf(0,0,2,2,2),
            intArrayOf(0,0,2,0,0),
            intArrayOf(-2,0,1,2,2),
            intArrayOf(0,0,0,2,0),
            intArrayOf(-2,0,0,2,0),
            intArrayOf(0,0,-1,2,1),
            intArrayOf(0,2,0,0,0),
            intArrayOf(2,0,-1,0,1),
            intArrayOf(-2,2,0,2,2),
            intArrayOf(0,1,0,0,1),
            intArrayOf(-2,0,1,0,1),
            intArrayOf(0,-1,0,0,1),
            intArrayOf(0,0,2,-2,0),
            intArrayOf(2,0,-1,2,1),
            intArrayOf(2,0,1,2,2),
            intArrayOf(0,1,0,2,2),
            intArrayOf(-2,1,1,0,0),
            intArrayOf(0,-1,0,2,2),
            intArrayOf(2,0,0,2,1),
            intArrayOf(2,0,1,0,0),
            intArrayOf(-2,0,2,2,2),
            intArrayOf(-2,0,1,2,1),
            intArrayOf(2,0,-2,0,1),
            intArrayOf(2,0,0,0,1),
            intArrayOf(0,-1,1,0,0),
            intArrayOf(-2,-1,0,2,1),
            intArrayOf(-2,0,0,0,1),
            intArrayOf(0,0,2,2,1),
            intArrayOf(-2,0,2,0,1),
            intArrayOf(-2,1,0,2,1),
            intArrayOf(0,0,1,-2,0),
            intArrayOf(-1,0,1,0,0),
            intArrayOf(-2,1,0,0,0),
            intArrayOf(1,0,0,0,0),
            intArrayOf(0,0,1,2,0),
            intArrayOf(0,0,-2,2,2),
            intArrayOf(-1,-1,1,0,0),
            intArrayOf(0,1,1,0,0),
            intArrayOf(0,-1,1,2,2),
            intArrayOf(2,-1,-1,2,2),
            intArrayOf(0,0,3,2,2),
            intArrayOf(2,-1,0,2,2)
        )

        private val PE_TERMS = arrayOf(
            doubleArrayOf(-171996.0,-174.2,92025.0,8.9),
            doubleArrayOf(-13187.0,-1.6,5736.0,-3.1),
            doubleArrayOf(-2274.0,-0.2,977.0,-0.5),
            doubleArrayOf(2062.0,0.2,-895.0,0.5),
            doubleArrayOf(1426.0,-3.4,54.0,-0.1),
            doubleArrayOf(712.0,0.1,-7.0,0.0),
            doubleArrayOf(-517.0,1.2,224.0,-0.6),
            doubleArrayOf(-386.0,-0.4,200.0,0.0),
            doubleArrayOf(-301.0,0.0,129.0,-0.1),
            doubleArrayOf(217.0,-0.5,-95.0,0.3),
            doubleArrayOf(-158.0,0.0,0.0,0.0),
            doubleArrayOf(129.0,0.1,-70.0,0.0),
            doubleArrayOf(123.0,0.0,-53.0,0.0),
            doubleArrayOf(63.0,0.0,0.0,0.0),
            doubleArrayOf(63.0,0.1,-33.0,0.0),
            doubleArrayOf(-59.0,0.0,26.0,0.0),
            doubleArrayOf(-58.0,-0.1,32.0,0.0),
            doubleArrayOf(-51.0,0.0,27.0,0.0),
            doubleArrayOf(48.0,0.0,0.0,0.0),
            doubleArrayOf(46.0,0.0,-24.0,0.0),
            doubleArrayOf(-38.0,0.0,16.0,0.0),
            doubleArrayOf(-31.0,0.0,13.0,0.0),
            doubleArrayOf(29.0,0.0,0.0,0.0),
            doubleArrayOf(29.0,0.0,-12.0,0.0),
            doubleArrayOf(26.0,0.0,0.0,0.0),
            doubleArrayOf(-22.0,0.0,0.0,0.0),
            doubleArrayOf(21.0,0.0,-10.0,0.0),
            doubleArrayOf(17.0,-0.1,0.0,0.0),
            doubleArrayOf(16.0,0.0,-8.0,0.0),
            doubleArrayOf(-16.0,0.1,7.0,0.0),
            doubleArrayOf(-15.0,0.0,9.0,0.0),
            doubleArrayOf(-13.0,0.0,7.0,0.0),
            doubleArrayOf(-12.0,0.0,6.0,0.0),
            doubleArrayOf(11.0,0.0,0.0,0.0),
            doubleArrayOf(-10.0,0.0,5.0,0.0),
            doubleArrayOf(-8.0,0.0,3.0,0.0),
            doubleArrayOf(7.0,0.0,-3.0,0.0),
            doubleArrayOf(-7.0,0.0,0.0,0.0),
            doubleArrayOf(-7.0,0.0,3.0,0.0),
            doubleArrayOf(-7.0,0.0,3.0,0.0),
            doubleArrayOf(6.0,0.0,0.0,0.0),
            doubleArrayOf(6.0,0.0,-3.0,0.0),
            doubleArrayOf(6.0,0.0,-3.0,0.0),
            doubleArrayOf(-6.0,0.0,3.0,0.0),
            doubleArrayOf(-6.0,0.0,3.0,0.0),
            doubleArrayOf(5.0,0.0,0.0,0.0),
            doubleArrayOf(-5.0,0.0,3.0,0.0),
            doubleArrayOf(-5.0,0.0,3.0,0.0),
            doubleArrayOf(-5.0,0.0,3.0,0.0),
            doubleArrayOf(4.0,0.0,0.0,0.0),
            doubleArrayOf(4.0,0.0,0.0,0.0),
            doubleArrayOf(4.0,0.0,0.0,0.0),
            doubleArrayOf(-4.0,0.0,0.0,0.0),
            doubleArrayOf(-4.0,0.0,0.0,0.0),
            doubleArrayOf(-4.0,0.0,0.0,0.0),
            doubleArrayOf(3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0),
            doubleArrayOf(-3.0,0.0,0.0,0.0)
        )
    }
}
