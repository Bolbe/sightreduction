package traps.celestnaveasy

import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.collections.ArrayList
import kotlin.math.abs

class DataSet(pointList: List<Pair<Double, Double>>) {

    private var data = Array(5) {DoubleArray(2)}

    var bestPoint : Pair<Double, Double>? = null
        private set
    var bestDistance = -1.0
        private set

    private var slope = 0.0
    private var intercept = 0.0
    private val sseList = ArrayList<Double>()
    private val distanceList = ArrayList<Double>()

    var bestPointInGroup = -1
        private set
    var bestPointGroup = listOf<Int>()
        private set

    init {
        if (pointList.size>4) {
            for ((i, point) in pointList.withIndex()) {
                data[i][0] = point.first
                data[i][1] = point.second
            }
            val reg = SimpleRegression()
            // for each possible group of 3 points, calculate regression and take sum squared error
            for (indexList in possiblePointGroup) {
                reg.clear()
                val localData = arrayOf(data[indexList[0]], data[indexList[1]], data[indexList[2]])
                reg.addData(localData)
                sseList.add(reg.sumSquaredErrors)
            }

            // take the group with the lowest error
            bestPointGroup = possiblePointGroup[sseList.indexOf(sseList.toDoubleArray().minOrNull())]
            val selectedPoints = arrayOf(
                data[bestPointGroup[0]],
                data[bestPointGroup[1]],
                data[bestPointGroup[2]]
            )
            reg.clear()
            // calculate regression with these 3 points
            reg.addData(selectedPoints)
            slope = reg.slope
            intercept = reg.intercept

            for (point in selectedPoints) {
                distanceList.add(abs(point[1]-(point[0]*slope+intercept)))
            }

            bestDistance = distanceList.toDoubleArray().minOrNull()?:-1.0
            bestPointInGroup = distanceList.indexOf(bestDistance)
            bestPoint = pointList.elementAtOrNull(bestPointGroup[bestPointInGroup])
        }
    }

    fun sseList() = sseList.toList()
    fun distanceList() = distanceList.toList()

    companion object {
        // All possible ways to take 3 points out of 5: C(5,30)=10
        private val possiblePointGroup = listOf(
            listOf(0,1,2),
            listOf(0,1,3),
            listOf(0,1,4),
            listOf(0,2,3),
            listOf(0,2,4),
            listOf(0,3,4),
            listOf(1,2,3),
            listOf(1,2,4),
            listOf(1,3,4),
            listOf(2,3,4)
        )
    }

}