package fr.hozakan.flysightcompanion.recordsmodule.business.analyze

import fr.hozakan.flysightcompanion.model.extensions.toEpochMillisecond
import fr.hozakan.flysightcompanion.model.records.AnalyzeOptions
import fr.hozakan.flysightcompanion.model.records.DataPoints
import fr.hozakan.flysightcompanion.model.records.GroundReference
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.toComputableDataPoint
import java.time.ZoneOffset

//class DefaultRecordAnalyzer2 : RecordAnalyzer {
//    override suspend fun analyze(dataPoints: DataPoints, options: AnalyzeOptions): RecordAnalyze {
//        if (dataPoints.isEmpty()) return RecordAnalyze.error("No data points to analyze")
//        val computableDataPoints = dataPoints.map { it.toComputableDataPoint() }
//        val dp0 = dataPoints.first()
//        val startTime = dp0.dateTime.toEpochMillisecond(ZoneOffset.UTC)
//        val dataPointsWithInitializedTime = computableDataPoints.map { dp ->
//            val dpTime = dp.dateTime.toEpochMillisecond(ZoneOffset.UTC)
//            dp.copy(
//                t = (dpTime - startTime) / 1000.0,
//            )
//        }
//
//        val groundReference = when (val groundReference = options.groundReference) {
//            GroundReference.Automatic -> dataPointsWithInitializedTime.last().hMSL
//            is GroundReference.Fixed -> groundReference.value
//        }
//        val dataPointsWithInitializedAltitude = dataPointsWithInitializedTime.map { dp ->
//            dp.copy(
//                z = dp.hMSL - groundReference
//            )
//        }
//
//        val dataPointsWithInitializedAcceleration = dataPointsWithInitializedAltitude.mapIndexed { index, dp ->
//            val accelN = 0.0
//            val accelE = 0.0
//            val accelD = 0.0
//        }
//    }
//}