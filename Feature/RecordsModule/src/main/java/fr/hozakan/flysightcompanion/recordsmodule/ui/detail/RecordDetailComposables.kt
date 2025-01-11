package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.records.ComputableDataPoint
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.course
import fr.hozakan.flysightcompanion.model.records.courseRate
import fr.hozakan.flysightcompanion.model.records.diveAngle
import fr.hozakan.flysightcompanion.model.records.energyRate
import fr.hozakan.flysightcompanion.model.records.glideRatio
import fr.hozakan.flysightcompanion.model.records.horizontalSpeed
import fr.hozakan.flysightcompanion.model.records.sep
import fr.hozakan.flysightcompanion.model.records.speedScoreAccuracy
import fr.hozakan.flysightcompanion.model.records.totalEnergy
import fr.hozakan.flysightcompanion.model.records.totalSpeed
import fr.hozakan.flysightcompanion.model.records.verticalSpeed
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem

@Composable
fun RecordDetailScreen(
    recordName: String
) {
    val factory = LocalViewModelFactory.current

    val viewModel: RecordDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(recordName) {
        viewModel.loadRecord(recordName)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            val analyze = state.analyze
            val displayableItems = state.plotLeftItems
            val plotDisplayPreferences = state.plotDisplayPreferences
            if (analyze.dataPoints.isNotEmpty()) {

                val minMSL = analyze.dataPoints.minOf { it.hMSL }
                val maxMSL = analyze.dataPoints.maxOf { it.hMSL }
                val minTime = analyze.dataPoints.minOf { it.t }
                val maxTime = analyze.dataPoints.maxOf { it.t }

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            val textColor = LocalContentColor.current
                            Column {
                                Text(text = "$maxMSL")
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "$minMSL")
                            }
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val padding = 16.dp.toPx()
                                val chartWidth = size.width - padding * 2
                                val chartHeight = size.height - padding * 2
                                val zeroWithOffset = Offset(padding, padding)

                                val topRight = zeroWithOffset + Offset(chartWidth, 0f)
                                val bottomLeft = zeroWithOffset + Offset(0f, chartHeight)
                                val bottomRight = zeroWithOffset + Offset(chartWidth, chartHeight)

                                drawLine(
                                    color = textColor,
                                    start = zeroWithOffset,
                                    end = bottomLeft
                                )
                                drawLine(
                                    color = textColor,
                                    start = bottomLeft,
                                    end = bottomRight
                                )
                                displayableItems.forEach { item ->
                                    when (item) {
                                        PlotLeftItem.Acceleration ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.accelerationColor
                                                    )
                                                )
                                            ) {
                                                it.accel
                                            }

                                        PlotLeftItem.Course ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.courseColor
                                                    )
                                                )
                                            ) {
                                                it.course
                                            }

                                        PlotLeftItem.CourseAccuracy -> drawPlot(
                                            analyze,
                                            minTime,
                                            maxTime,
                                            chartWidth,
                                            chartHeight,
                                            zeroWithOffset,
                                            Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.courseAccuracyColor
                                                )
                                            )
                                        ) {
                                            it.cAcc
                                        }

                                        PlotLeftItem.CourseRate ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.courseRateColor
                                                    )
                                                )
                                            ) {
                                                it.courseRate
                                            }

                                        PlotLeftItem.DiveAngle ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.diveAngleColor
                                                    )
                                                )
                                            ) {
                                                it.diveAngle
                                            }

                                        PlotLeftItem.DiveRate ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.diveRateColor
                                                    )
                                                )
                                            ) {
                                                it.curv
                                            }

                                        PlotLeftItem.DragCoefficient ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.dragCoefficientColor
                                                    )
                                                )
                                            ) {
                                                it.drag
                                            }

                                        PlotLeftItem.Elevation ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.elevationColor
                                                    )
                                                )
                                            ) {
                                                it.hMSL
                                            }

                                        PlotLeftItem.EnergyRate ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.energyRateColor
                                                    )
                                                )
                                            ) {
                                                it.energyRate
                                            }

                                        PlotLeftItem.GlideRatio ->
                                            drawPlot(
                                                analyze,
                                                minTime,
                                                maxTime,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.glideRatioColor
                                                    )
                                                )
                                            ) {
                                                it.glideRatio
                                            }

                                        PlotLeftItem.HorizontalAccuracy -> drawPlot(
                                            analyze,
                                            minTime,
                                            maxTime,
                                            chartWidth,
                                            chartHeight,
                                            zeroWithOffset,
                                            Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.horizontalAccuracyColor
                                                )
                                            )
                                        ) {
                                            it.hAcc
                                        }

                                        PlotLeftItem.HorizontalSpeed ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.horizontalSpeedColor
                                                )
                                            )) {
                                                it.horizontalSpeed
                                            }

                                        PlotLeftItem.LiftCoefficient ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.liftCoefficientColor
                                                )
                                            )) {
                                                it.lift
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationDown ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.accelerationDownColor
                                                )
                                            )) {
                                                it.az
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationForward ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.accelerationForwardColor
                                                )
                                            )) {
                                                it.ax
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationMagnitude ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.accelerationMagnitudeColor
                                                )
                                            )) {
                                                it.amag
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationRight -> drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                            android.graphics.Color.parseColor(
                                                plotDisplayPreferences.accelerationRightColor
                                            )
                                        )) {
                                            it.ay
                                        }

                                        PlotLeftItem.NumberOfSatellites ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.numberOfSatellitesColor
                                                )
                                            )) {
                                                it.numSV.toDouble()
                                            }

                                        PlotLeftItem.Speed.SpeedScoreAccuracy ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.speedScoreAccuracyColor
                                                )
                                            )) {
                                                it.speedScoreAccuracy
                                            }

                                        PlotLeftItem.SpeedAccuracy ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.speedAccuracyColor
                                                )
                                            )) {
                                                it.sAcc
                                            }

                                        PlotLeftItem.TotalEnergy ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.totalEnergyColor
                                                )
                                            )) {
                                                it.totalEnergy
                                            }

                                        PlotLeftItem.TotalSpeed ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.totalSpeedColor
                                                )
                                            )) {
                                                it.totalSpeed
                                            }

                                        PlotLeftItem.VerticalAccuracy ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.verticalAccuracyColor
                                                )
                                            )) {
                                                it.vAcc
                                            }

                                        PlotLeftItem.VerticalSpeed ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.verticalSpeedColor
                                                )
                                            )) {
                                                it.verticalSpeed
                                            }

                                        PlotLeftItem.Wingsuit.SphericalErrorProbability ->
                                            drawPlot(analyze, minTime, maxTime, chartWidth, chartHeight, zeroWithOffset, Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.sphericalErrorProbabilityColor
                                                )
                                            )) {
                                                it.sep
                                            }
                                    }
                                }
                            }
                        }
                        Row {
                            Text(text = "$minTime")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "$maxTime")
                        }
                    }
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun DrawScope.drawPlot(
    analyze: RecordAnalyze,
    minTime: Double,
    maxTime: Double,
    chartWidth: Float,
    chartHeight: Float,
    zeroWithOffset: Offset,
    color: Color,
    valuePicker: (ComputableDataPoint) -> Double
) {
    val minValue = analyze.dataPoints.minOf { valuePicker(it) }
    val maxValue = analyze.dataPoints.maxOf { valuePicker(it) }
    drawPoints(
        points = analyze.dataPoints.map { dp ->
            val x = (dp.t - minTime) / (maxTime - minTime) * chartWidth
            val y =
                chartHeight - ((valuePicker(dp) - minValue) / (maxValue - minValue) * chartHeight)
            Offset(x.toFloat(), y.toFloat()) + zeroWithOffset
        },
        pointMode = PointMode.Polygon,
        color = color
    )
}