package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.extension.distanceInUnit
import fr.hozakan.flysightcompanion.model.config.UnitSystem
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
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem
import fr.hozakan.flysightcompanion.model.ui.accelerationColor
import fr.hozakan.flysightcompanion.model.ui.accelerationDownColor
import fr.hozakan.flysightcompanion.model.ui.accelerationForwardColor
import fr.hozakan.flysightcompanion.model.ui.accelerationMagnitudeColor
import fr.hozakan.flysightcompanion.model.ui.accelerationRightColor
import fr.hozakan.flysightcompanion.model.ui.courseAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.courseColor
import fr.hozakan.flysightcompanion.model.ui.courseRateColor
import fr.hozakan.flysightcompanion.model.ui.diveAngleColor
import fr.hozakan.flysightcompanion.model.ui.diveRateColor
import fr.hozakan.flysightcompanion.model.ui.dragCoefficientColor
import fr.hozakan.flysightcompanion.model.ui.elevationColor
import fr.hozakan.flysightcompanion.model.ui.energyRateColor
import fr.hozakan.flysightcompanion.model.ui.glideRatioColor
import fr.hozakan.flysightcompanion.model.ui.horizontalAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.horizontalSpeedColor
import fr.hozakan.flysightcompanion.model.ui.liftCoefficientColor
import fr.hozakan.flysightcompanion.model.ui.numberOfSatellitesColor
import fr.hozakan.flysightcompanion.model.ui.speedAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.speedScoreAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.sphericalErrorProbabilityColor
import fr.hozakan.flysightcompanion.model.ui.totalEnergyColor
import fr.hozakan.flysightcompanion.model.ui.totalSpeedColor
import fr.hozakan.flysightcompanion.model.ui.verticalAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.verticalSpeedColor
import fr.hozakan.flysightcompanion.designsystem.R
import timber.log.Timber
import kotlin.math.abs

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

                val xValuePicker = when (state.plotBottomItem) {
                    PlotBottomItem.Time -> { it: ComputableDataPoint -> it.t }
                    PlotBottomItem.HorizontalDistance -> { it: ComputableDataPoint -> it.dist2D }
                    PlotBottomItem.TotalDistance -> { it: ComputableDataPoint -> it.dist3D }
                }

                val yValuePicker = when (state.plotLeftItems.first()) {
                    PlotLeftItem.Acceleration -> { it: ComputableDataPoint -> it.accel }
                    PlotLeftItem.Course -> { it: ComputableDataPoint -> it.course }
                    PlotLeftItem.CourseAccuracy -> { it: ComputableDataPoint -> it.cAcc }
                    PlotLeftItem.CourseRate -> { it: ComputableDataPoint -> it.courseRate }
                    PlotLeftItem.DiveAngle -> { it: ComputableDataPoint -> it.diveAngle }
                    PlotLeftItem.DiveRate -> { it: ComputableDataPoint -> it.curv }
                    PlotLeftItem.DragCoefficient -> { it: ComputableDataPoint -> it.drag }
                    PlotLeftItem.Elevation -> { it: ComputableDataPoint -> it.hMSL }
                    PlotLeftItem.EnergyRate -> { it: ComputableDataPoint -> it.energyRate }
                    PlotLeftItem.GlideRatio -> { it: ComputableDataPoint -> it.glideRatio }
                    PlotLeftItem.HorizontalAccuracy -> { it: ComputableDataPoint -> it.hAcc }
                    PlotLeftItem.HorizontalSpeed -> { it: ComputableDataPoint -> it.horizontalSpeed }
                    PlotLeftItem.LiftCoefficient -> { it: ComputableDataPoint -> it.lift }
                    PlotLeftItem.LocalAcceleration.AccelerationDown -> { it: ComputableDataPoint -> it.az }
                    PlotLeftItem.LocalAcceleration.AccelerationForward -> { it: ComputableDataPoint -> it.ax }
                    PlotLeftItem.LocalAcceleration.AccelerationMagnitude -> { it: ComputableDataPoint -> it.amag }
                    PlotLeftItem.LocalAcceleration.AccelerationRight -> { it: ComputableDataPoint -> it.ay }
                    PlotLeftItem.NumberOfSatellites -> { it: ComputableDataPoint -> it.numSV.toDouble() }
                    PlotLeftItem.Speed.SpeedScoreAccuracy -> { it: ComputableDataPoint -> it.speedScoreAccuracy }
                    PlotLeftItem.SpeedAccuracy -> { it: ComputableDataPoint -> it.sAcc }
                    PlotLeftItem.TotalEnergy -> { it: ComputableDataPoint -> it.totalEnergy }
                    PlotLeftItem.TotalSpeed -> { it: ComputableDataPoint -> it.totalSpeed }
                    PlotLeftItem.VerticalAccuracy -> { it: ComputableDataPoint -> it.vAcc }
                    PlotLeftItem.VerticalSpeed -> { it: ComputableDataPoint -> it.verticalSpeed }
                    PlotLeftItem.Wingsuit.SphericalErrorProbability -> { it: ComputableDataPoint -> it.sep }
                }

                val yMin = analyze.dataPoints.minOf { yValuePicker(it) }
                val yMax = analyze.dataPoints.maxOf { yValuePicker(it) }
                val xMin = analyze.dataPoints.minOf { xValuePicker(it) }
                val xMax = analyze.dataPoints.maxOf { xValuePicker(it) }


                val closestToZero = analyze.dataPoints.minBy {
//                    Timber.d("Hoz2 it.t: ${it.t}, x = ${it.x}, y = ${it.y}")
                    abs(it.t)
                }
                val around250 = analyze.dataPoints.minBy {
                    abs(it.t - 250.0)
                }
                val around200 = analyze.dataPoints.minBy {
                    abs(it.t - 200.0)
                }
                Timber.d(
                    "Hoz2 closestToZero: x=${xValuePicker(closestToZero)}, y=${
                        yValuePicker(
                            closestToZero
                        )
                    } ${closestToZero.dateTime}"
                )
                Timber.d("Hoz2 around250: x=${xValuePicker(around250)}, y=${yValuePicker(around250)} ${around250.dateTime}")
                Timber.d("Hoz2 around200: x=${xValuePicker(around200)}, y=${yValuePicker(around200)} ${around200.dateTime}")
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
                                Text(text = "$yMax")
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = "$yMin")
                            }

                            val textMeasurer = rememberTextMeasurer()
                            val context = LocalContext.current
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(width = 2.dp, color = Color.Green)
                            ) {
                                val padding = 16.dp.toPx()
                                val chartWidth = size.width - padding * 2
                                val bottomScaleHeight = 56.dp.toPx()
                                val chartHeight = size.height - padding * 2 - bottomScaleHeight
                                val zeroWithOffset = Offset(padding, padding)
                                val xScale = (chartWidth / (xMax - xMin)).toFloat()

                                val topRight = zeroWithOffset + Offset(chartWidth, 0f)
                                val bottomLeft = zeroWithOffset + Offset(0f, chartHeight)
                                val bottomRight = zeroWithOffset + Offset(chartWidth, chartHeight)

                                val xPosition =
                                    zeroWithOffset.x - (xMin - xValuePicker(closestToZero)) * xScale
                                val zeroOffset = Offset(
//                                    x = (zeroWithOffset.x + closestToZero.t * xScale).toFloat(),
                                    x = xPosition.toFloat(),
                                    y = zeroWithOffset.y + chartHeight - ((yValuePicker(
                                        closestToZero
                                    ) - yMin) / (yMax - yMin) * chartHeight).toFloat()
                                )
                                drawCircle(
                                    color = Color.Red,
                                    radius = 6f,
                                    center = zeroOffset
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(x = xPosition.toFloat(), y = zeroWithOffset.y),
                                    end = Offset(
                                        x = xPosition.toFloat(),
                                        y = zeroWithOffset.y + chartHeight
                                    )
                                )

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
                                val plotBottomItem = state.plotBottomItem
                                drawBottomScale(
                                    context = context,
                                    bottomItem = plotBottomItem,
                                    analyze = state.analyze,
                                    textMeasurer = textMeasurer,
                                    width = chartWidth,
                                    height = bottomScaleHeight,
                                    chartWidth = chartWidth,
                                    chartHeight = chartHeight,
                                    zeroWithOffset = zeroWithOffset,
                                    textColor = textColor,
                                    unitSystem = state.unitSystem
                                )
                                displayableItems.forEach { item ->
                                    when (item) {
                                        PlotLeftItem.Acceleration ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.accelerationColor
                                                    )
                                                ),
                                                {
                                                    it.accel
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.Course ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.courseColor
                                                    )
                                                ),
                                                {
                                                    it.course
                                                }

                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.CourseAccuracy -> drawPlot(
                                            analyze,
                                            xMin,
                                            xMax,
                                            xScale,
                                            chartWidth,
                                            chartHeight,
                                            zeroWithOffset,
                                            Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.courseAccuracyColor
                                                )
                                            ),
                                            {
                                                it.cAcc
                                            }
                                        ) {
                                            it.t
                                        }

                                        PlotLeftItem.CourseRate ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.courseRateColor
                                                    )
                                                ),
                                                {
                                                    it.courseRate
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.DiveAngle ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.diveAngleColor
                                                    )
                                                ),
                                                {
                                                    it.diveAngle
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.DiveRate ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.diveRateColor
                                                    )
                                                ),
                                                {
                                                    it.curv
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.DragCoefficient ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.dragCoefficientColor
                                                    )
                                                ),
                                                {
                                                    it.drag
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.Elevation ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.elevationColor
                                                    )
                                                ),
                                                {
                                                    it.hMSL
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.EnergyRate ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.energyRateColor
                                                    )
                                                ),
                                                {
                                                    it.energyRate
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.GlideRatio ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.glideRatioColor
                                                    )
                                                ),
                                                {
                                                    it.glideRatio
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.HorizontalAccuracy -> drawPlot(
                                            analyze,
                                            xMin,
                                            xMax,
                                            xScale,
                                            chartWidth,
                                            chartHeight,
                                            zeroWithOffset,
                                            Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.horizontalAccuracyColor
                                                )
                                            ),
                                            {
                                                it.hAcc
                                            }
                                        ) {
                                            it.t
                                        }

                                        PlotLeftItem.HorizontalSpeed ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.horizontalSpeedColor
                                                    )
                                                ),
                                                {
                                                    it.horizontalSpeed
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.LiftCoefficient ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.liftCoefficientColor
                                                    )
                                                ),
                                                {
                                                    it.lift
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationDown ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.accelerationDownColor
                                                    )
                                                ),
                                                {
                                                    it.az
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationForward ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.accelerationForwardColor
                                                    )
                                                ),
                                                {
                                                    it.ax
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationMagnitude ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.accelerationMagnitudeColor
                                                    )
                                                ),
                                                {
                                                    it.amag
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.LocalAcceleration.AccelerationRight -> drawPlot(
                                            analyze,
                                            xMin,
                                            xMax,
                                            xScale,
                                            chartWidth,
                                            chartHeight,
                                            zeroWithOffset,
                                            Color(
                                                android.graphics.Color.parseColor(
                                                    plotDisplayPreferences.accelerationRightColor
                                                )
                                            ),
                                            {
                                                it.ay
                                            }

                                        ) {
                                            it.t
                                        }

                                        PlotLeftItem.NumberOfSatellites ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.numberOfSatellitesColor
                                                    )
                                                ),
                                                {
                                                    it.numSV.toDouble()
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.Speed.SpeedScoreAccuracy ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.speedScoreAccuracyColor
                                                    )
                                                ),
                                                {
                                                    it.speedScoreAccuracy
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.SpeedAccuracy ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.speedAccuracyColor
                                                    )
                                                ),
                                                {
                                                    it.sAcc
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.TotalEnergy ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.totalEnergyColor
                                                    )
                                                ),
                                                {
                                                    it.totalEnergy
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.TotalSpeed ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.totalSpeedColor
                                                    )
                                                ),
                                                {
                                                    it.totalSpeed
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.VerticalAccuracy ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.verticalAccuracyColor
                                                    )
                                                ),
                                                {
                                                    it.vAcc
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.VerticalSpeed ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.verticalSpeedColor
                                                    )
                                                ),
                                                {
                                                    it.verticalSpeed
                                                }
                                            ) {
                                                it.t
                                            }

                                        PlotLeftItem.Wingsuit.SphericalErrorProbability ->
                                            drawPlot(
                                                analyze,
                                                xMin,
                                                xMax,
                                                xScale,
                                                chartWidth,
                                                chartHeight,
                                                zeroWithOffset,
                                                Color(
                                                    android.graphics.Color.parseColor(
                                                        plotDisplayPreferences.sphericalErrorProbabilityColor
                                                    )
                                                ),
                                                {
                                                    it.sep
                                                }
                                            ) {
                                                it.t
                                            }
                                    }
                                }
                            }
                        }
                        Row {
                            Text(text = "$xMin")
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "$xMax")
                        }
                    }
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun DrawScope.drawBottomScale(
    context: Context,
    unitSystem: UnitSystem,
    bottomItem: PlotBottomItem,
    analyze: RecordAnalyze,
    textMeasurer: TextMeasurer,
    width: Float,
    height: Float,
    chartWidth: Float,
    chartHeight: Float, 
    zeroWithOffset: Offset,
    textColor: Color
) {
    when (bottomItem) {
        PlotBottomItem.Time -> {
            drawTimeBottomScale(
                context,
                zeroWithOffset,
                chartHeight,
                chartWidth,
                analyze,
                height,
                width,
                textMeasurer,
                textColor
            )
        }

        PlotBottomItem.HorizontalDistance -> {
            drawHorizontalDistanceBottomScale(
                context,
                zeroWithOffset,
                chartHeight,
                chartWidth,
                analyze,
                height,
                width,
                textMeasurer,
                textColor,
                unitSystem
            )
        }

        PlotBottomItem.TotalDistance -> {
            drawTotalDistanceBottomScale(
                context,
                zeroWithOffset,
                chartHeight,
                chartWidth,
                analyze,
                height,
                width,
                textMeasurer,
                textColor,
                unitSystem
            )
        }
    }
}

private fun DrawScope.drawHorizontalDistanceBottomScale(
    context: Context,
    zeroWithOffset: Offset,
    chartHeight: Float,
    chartWidth: Float,
    analyze: RecordAnalyze,
    height: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color,
    unitSystem: UnitSystem
) {
    val firstDp = analyze.dataPoints.first()
    val lastDp = analyze.dataPoints.last()
    val distance = lastDp.dist2D - firstDp.dist2D
    drawSpecificBottomScale(
        zeroWithOffset,
        chartHeight,
        chartWidth,
        analyze,
        height,
        width,
        textMeasurer,
        textColor,
        {
            it.dist3D.distanceInUnit(unitSystem)
        },
//        (distance / 5.0).toInt(),
        500,
        context.getString(
            R.string.record_detail_horizontal_distance,
            context.getString(unitSystem.distanceTextResource)
        )
    )
}

private fun DrawScope.drawTotalDistanceBottomScale(
    context: Context,
    zeroWithOffset: Offset,
    chartHeight: Float,
    chartWidth: Float,
    analyze: RecordAnalyze,
    height: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color,
    unitSystem: UnitSystem
) {
    val firstDp = analyze.dataPoints.first()
    val lastDp = analyze.dataPoints.last()
    val distance = lastDp.dist3D - firstDp.dist3D
    drawSpecificBottomScale(
        zeroWithOffset,
        chartHeight,
        chartWidth,
        analyze,
        height,
        width,
        textMeasurer,
        textColor,
        {
            it.dist3D.distanceInUnit(unitSystem)
        },
//        (distance / 5.0).toInt(),
        500,
        context.getString(
            R.string.record_detail_horizontal_distance,
            context.getString(unitSystem.distanceTextResource)
        )
    )
}

private fun DrawScope.drawTimeBottomScale(
    context: Context,
    zeroWithOffset: Offset,
    chartHeight: Float,
    chartWidth: Float,
    analyze: RecordAnalyze,
    height: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color
) {
    drawSpecificBottomScale(
        zeroWithOffset,
        chartHeight,
        chartWidth,
        analyze,
        height,
        width,
        textMeasurer,
        textColor,
        {
            it.t
        },
        50,
        context.getString(R.string.record_detail_time)
    )
}

private fun DrawScope.drawSpecificBottomScale(
    zeroWithOffset: Offset,
    chartHeight: Float,
    chartWidth: Float,
    analyze: RecordAnalyze,
    height: Float,
    width: Float,
    textMeasurer: TextMeasurer,
    textColor: Color,
    xValuePicker: (ComputableDataPoint) -> Double,
    stepValue: Int,
    label: String
) {
    val bigGraduationHeight = 16.dp.toPx()
    val smallGraduationHeight = bigGraduationHeight * 0.5f
    val maxPoint = analyze.dataPoints.maxBy { it.t }
    val minPoint = analyze.dataPoints.minBy { it.t }
    val maxValue = maxPoint.t //xValuePicker(maxPoint)
    val minValue = minPoint.t //xValuePicker(minPoint)
    val xScale = width / (maxPoint.t - minPoint.t)
    if (0.0 in minValue..maxValue) {
        for (i in 0 until maxPoint.t.toInt() step stepValue) {
            val xPosition = zeroWithOffset.x - (minPoint.t - i) * xScale
//                    Timber.d("Hoz2 zeroWithOffset.x - (minTime.t - i) * xScale = ${zeroWithOffset.x} - (${minTime.t} - $i) * $xScale")
//                    Timber.d("Hoz2 = ${zeroWithOffset.x} - (${minTime.t - i}) * $xScale")
//                    Timber.d("Hoz2 = ${zeroWithOffset.x} - ${(minTime.t - i) * xScale}")
//                    Timber.d("Hoz2 = ${zeroWithOffset.x - (minTime.t - i) * xScale}")
            if ((i / stepValue.toDouble()).mod(5.0) == 0.0) {
                val measuredText = textMeasurer.measure(
                    text = "$i"/*,
                    constraints = Constraints.fixedWidth((size.width * 2f / 3f).toInt())*/
                )
                drawText(
                    measuredText,
                    color = textColor,
                    topLeft = Offset(
                        xPosition.toFloat() - measuredText.size.width / 2,
                        zeroWithOffset.y + chartHeight + 8.dp.toPx()
                    )
                )
            }
            drawLine(
                color = textColor,
                start = Offset(
                    xPosition.toFloat(),
                    zeroWithOffset.y + chartHeight
                ),
                end = Offset(
                    xPosition.toFloat(),
                    zeroWithOffset.y + chartHeight - if ((i / stepValue.toDouble()).mod(5.0) == 0.0) bigGraduationHeight else smallGraduationHeight
                )
            )
        }
        for (i in stepValue until abs(minValue.toInt()) step stepValue) {
            val xPosition = zeroWithOffset.x - (minValue + i) * xScale
            if ((i / stepValue.toDouble()).mod(5.0) == 0.0) {
                val measuredText = textMeasurer.measure(
                    text = "-$i"/*,
                    constraints = Constraints.fixedWidth((size.width * 2f / 3f).toInt())*/
                )
                drawText(
                    measuredText,
                    color = textColor,
                    topLeft = Offset(
                        xPosition.toFloat() - measuredText.size.width / 2,
                        zeroWithOffset.y + chartHeight + 8.dp.toPx()
                    )
                )
            }
            drawLine(
                color = textColor,
                start = Offset(
                    xPosition.toFloat(),
                    zeroWithOffset.y + chartHeight
                ),
                end = Offset(
                    xPosition.toFloat(),
                    zeroWithOffset.y + chartHeight - if ((i / stepValue.toDouble()).mod(5.0) == 0.0) bigGraduationHeight else smallGraduationHeight
                )
            )
        }
        val textZero = textMeasurer.measure("0")
        val timeText = textMeasurer.measure(
            text = label,
            constraints = Constraints.fixedHeight(height.toInt() - textZero.size.height)
        )
        drawText(
            timeText,
            color = textColor,
            topLeft = Offset(
                zeroWithOffset.x + chartWidth / 2 - timeText.size.width / 2,
                zeroWithOffset.y + chartHeight + 16.dp.toPx() + textZero.size.height
            )
        )
    }
}

private fun DrawScope.drawPlot(
    analyze: RecordAnalyze,
    minAbscissaValue: Double,
    maxAbscissaValue: Double,
    xScale: Float,
    chartWidth: Float,
    chartHeight: Float,
    zeroWithOffset: Offset,
    color: Color,
    valuePicker: (ComputableDataPoint) -> Double,
    abscissaValuePicker: (ComputableDataPoint) -> Double
) {
    val minValue = analyze.dataPoints.minOf { valuePicker(it) }
    val maxValue = analyze.dataPoints.maxOf { valuePicker(it) }
    drawPoints(
        points = analyze.dataPoints.map { dp ->
//            val x = (dp.t - minTime) / (maxTime - minTime) * chartWidth
            val x =
                (abscissaValuePicker(dp) - minAbscissaValue) / (maxAbscissaValue - minAbscissaValue) * chartWidth
            val y =
                chartHeight - (valuePicker(dp) - minValue) / (maxValue - minValue) * chartHeight
            Offset(x.toFloat(), y.toFloat()) + zeroWithOffset
        },
        pointMode = PointMode.Polygon,
        color = color
    )
}