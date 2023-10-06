package traps.celestnaveasy

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

object CorrectionTableRefraction {
    private val elevation =  doubleArrayOf( 03.0,  04.0,  05.0,  06.0,  07.0,  08.0,  09.0,  10.0,  11.0,  12.0,  13.0,  14.0,  15.0,  16.0,  17.0,  18.0,  19.5,  21.0,  22.5,  23.5,  24.5,  25.5,  26.5,  28.0, 29.0, 31.0, 32.5, 34.0, 36.0, 38.5, 41.0, 44.0, 47.0, 50.5, 55.0, 59.0, 64.5, 70.0, 76.5, 83.0, 90.0)
    private val correction = doubleArrayOf(-13.5, -11.2, -09.5, -08.3,  -7.3,  -6.5,  -5.8,  -5.3,  -4.8,  -4.5,  -4.1,  -3.8,  -3.4,  -3.2,  -3.0,  -2.8,  -2.6,  -2.4,  -2.2,  -2.1,  -2.0,  -1.9,  -1.8,  -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1, -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1, 0.0)

    private var polynomialSplineFunction: PolynomialSplineFunction =
        LinearInterpolator().interpolate(elevation, correction)

    fun correctionForElevation(elevation: Double) : Double {
        var localElevation = elevation
        if (localElevation<3.0) localElevation = 3.0
        if (localElevation>90.0) localElevation = 90.0
        return polynomialSplineFunction.value(localElevation)
    }

}