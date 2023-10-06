package traps.celestnaveasy

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

object CorrectionTableDip {
    private val eyeHeight =  doubleArrayOf(
         0.0, 1.03, 1.15,
        1.28, 1.40, 1.54, 1.68, 1.83, 1.99, 2.15, 2.32, 2.49, 2.68,
        2.87, 3.06, 3.26, 3.47, 3.68, 3.90, 4.13, 4.36, 4.60, 4.84,
        5.10, 5.35, 5.61, 5.88, 6.17, 6.45, 6.74, 7.04, 7.34, 7.64,
        7.96, 8.28, 8.61, 8.94, 9.29, 9.63, 9.98, 10.34, 10.71, 11.08,
        11.46, 11.84, 12.23, 12.63, 13.04, 13.45, 13.86, 14.29, 14.72, 15.15,
        15.59, 16.05, 16.50, 16.96, 17.43, 17.90, 18.38, 18.87, 19.37, 19.87,
        20.37, 20.89, 21.40, 21.93, 22.46, 22.99, 23.54, 24.09, 24.65, 25.22,
        25.78, 26.94, 28.12, 29.33, 30.57, 31.82
    )
    private val correction = doubleArrayOf(
         0.0, -1.8, -1.9,
        -2.0, -2.1, -2.2, -2.3, -2.4, -2.5, -2.6, -2.7, -2.8, -2.9,
        -3.0, -3.1, -3.2, -3.3, -3.4, -3.5, -3.6, -3.7, -3.8, -3.9,
        -4.0, -4.1, -4.2, -4.3, -4.4, -4.5, -4.6, -4.7, -4.8, -4.9,
        -5.0, -5.1, -5.2, -5.3, -5.4, -5.5, -5.6, -5.7, -5.8, -5.9,
        -6.0, -6.1, -6.2, -6.3, -6.4, -6.5, -6.6, -6.7, -6.8, -6.9,
        -7.0, -7.1, -7.2, -7.3, -7.4, -7.5, -7.6, -7.7, -7.8, -7.9,
        -8.0, -8.1, -8.2, -8.3, -8.4, -8.5, -8.6, -8.7, -8.8, -8.9,
        -9.0, -9.2, -9.4, -9.6, -9.8, -10.0
    )

    private var polynomialSplineFunction: PolynomialSplineFunction =
        LinearInterpolator().interpolate(eyeHeight, correction)

    fun correctionForHeight(height: Double) : Double {
        var localHeight = height
        if (localHeight<0) localHeight = 0.0
        if (localHeight>31.82) localHeight = 31.82
        return polynomialSplineFunction.value(localHeight)
    }

}