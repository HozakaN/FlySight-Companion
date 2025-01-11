package fr.hozakan.flysightcompanion.model.ui

typealias ColorHex = String

data class PlotDisplayPreferences(
    val elevationColor: ColorHex,
    val horizontalSpeedColor: ColorHex,
    val verticalSpeedColor: ColorHex,
    val totalSpeedColor: ColorHex,
    val courseColor: ColorHex,
    val courseRateColor: ColorHex,
    val courseAccuracyColor: ColorHex,
    val glideRatioColor: ColorHex,
    val diveAngleColor: ColorHex,
    val diveRateColor: ColorHex,
    val horizontalAccuracyColor: ColorHex,
    val verticalAccuracyColor: ColorHex,
    val speedAccuracyColor: ColorHex,
    val numberOfSatellitesColor: ColorHex,
    val accelerationColor: ColorHex,
    val accelerationForwardColor: ColorHex,
    val accelerationRightColor: ColorHex,
    val accelerationDownColor: ColorHex,
    val accelerationMagnitudeColor: ColorHex,
    val totalEnergyColor: ColorHex,
    val energyRateColor: ColorHex,
    val liftCoefficientColor: ColorHex,
    val dragCoefficientColor: ColorHex,
    val sphericalErrorProbabilityColor: ColorHex,
    val speedScoreAccuracyColor: ColorHex,
) {
    fun toStringPreference(): String {
        return "$elevationColor;$horizontalSpeedColor;$verticalSpeedColor;" +
                "$totalSpeedColor;$courseColor;$courseRateColor;$courseAccuracyColor;" +
                "$glideRatioColor;$diveAngleColor;$diveRateColor;$horizontalAccuracyColor;" +
                "$verticalAccuracyColor;$speedAccuracyColor;$numberOfSatellitesColor;" +
                "$accelerationColor;$accelerationForwardColor;$accelerationRightColor;" +
                "$accelerationDownColor;$accelerationMagnitudeColor;$totalEnergyColor;" +
                "$energyRateColor;$liftCoefficientColor;$dragCoefficientColor;" +
                "$sphericalErrorProbabilityColor;$speedScoreAccuracyColor"
    }

    companion object {
        fun fromStringPreference(prefStr: String): PlotDisplayPreferences? {
            val colors = prefStr.split(";")
            return try {
                PlotDisplayPreferences(
                    elevationColor = colors[0],
                    horizontalSpeedColor = colors[1],
                    verticalSpeedColor = colors[2],
                    totalSpeedColor = colors[3],
                    courseColor = colors[4],
                    courseRateColor = colors[5],
                    courseAccuracyColor = colors[6],
                    glideRatioColor = colors[7],
                    diveAngleColor = colors[8],
                    diveRateColor = colors[9],
                    horizontalAccuracyColor = colors[10],
                    verticalAccuracyColor = colors[11],
                    speedAccuracyColor = colors[12],
                    numberOfSatellitesColor = colors[13],
                    accelerationColor = colors[14],
                    accelerationForwardColor = colors[15],
                    accelerationRightColor = colors[16],
                    accelerationDownColor = colors[17],
                    accelerationMagnitudeColor = colors[18],
                    totalEnergyColor = colors[19],
                    energyRateColor = colors[20],
                    liftCoefficientColor = colors[21],
                    dragCoefficientColor = colors[22],
                    sphericalErrorProbabilityColor = colors[23],
                    speedScoreAccuracyColor = colors[24]
                )
            } catch (ex: IndexOutOfBoundsException)  {
                defaultDisplayPreferences
            }
        }
    }
}

val defaultDisplayPreferences = PlotDisplayPreferences(
    elevationColor = "#FF0000",
    horizontalSpeedColor = "#00FF00",
    verticalSpeedColor = "#0000FF",
    totalSpeedColor = "#FF00FF",
    courseColor = "#FFFF00",
    courseRateColor = "#00FFFF",
    courseAccuracyColor = "#FF8000",
    glideRatioColor = "#FF0080",
    diveAngleColor = "#00FF80",
    diveRateColor = "#8000FF",
    horizontalAccuracyColor = "#FF8080",
    verticalAccuracyColor = "#80FF80",
    speedAccuracyColor = "#8080FF",
    numberOfSatellitesColor = "#FF80FF",
    accelerationColor = "#80FF00",
    accelerationForwardColor = "#FF80FF",
    accelerationRightColor = "#80FFFF",
    accelerationDownColor = "#FF0080",
    accelerationMagnitudeColor = "#FF8000",
    totalEnergyColor = "#00FF80",
    energyRateColor = "#8000FF",
    liftCoefficientColor = "#FF8080",
    dragCoefficientColor = "#80FF80",
    sphericalErrorProbabilityColor = "#8080FF",
    speedScoreAccuracyColor = "#FF80FF",
)