package fr.hozakan.flysightcompanion.composablecommons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.speedTextResource
import fr.hozakan.flysightcompanion.framework.extension.distanceInUnit
import fr.hozakan.flysightcompanion.framework.extension.fromDistanceUnitToMeter
import fr.hozakan.flysightcompanion.framework.extension.fromSpeedUnitToCmPerSec
import fr.hozakan.flysightcompanion.framework.extension.speedInUnit
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem

@Composable
fun toneMinimumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        stringResource(R.string.label_minimum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.VerticalSpeed -> {
        stringResource(R.string.label_minimum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.GlideRatio -> {
        stringResource(R.string.label_minimum_glide_ratio)
    }

    ToneMode.InverseGlideRatio -> {
        stringResource(R.string.label_minimum_inverse_glide_ratio)
    }

    ToneMode.TotalSpeed -> {
        stringResource(R.string.label_minimum_total_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.DiveAngle -> {
        stringResource(R.string.label_minimum_dive_angle)
    }
}

@Composable
fun toneMaximumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        stringResource(R.string.label_maximum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.VerticalSpeed -> {
        stringResource(R.string.label_maximum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.GlideRatio -> {
        stringResource(R.string.label_maximum_glide_ratio)
    }

    ToneMode.InverseGlideRatio -> {
        stringResource(R.string.label_maximum_inverse_glide_ratio)
    }

    ToneMode.TotalSpeed -> {
        stringResource(R.string.label_maximum_total_speed, stringResource(unitSystem.speedTextResource))
    }

    ToneMode.DiveAngle -> {
        stringResource(R.string.label_maximum_dive_angle)
    }
}

fun Int.valueForToneMode(toneMode: ToneMode, unitSystem: UnitSystem): Int = when (toneMode) {
    ToneMode.HorizontalSpeed,
    ToneMode.VerticalSpeed,
    ToneMode.TotalSpeed -> {
        this.speedInUnit(unitSystem)
    }
    ToneMode.GlideRatio,
    ToneMode.InverseGlideRatio,
    ToneMode.DiveAngle -> {
        this
    }
}

fun Int.valueFromToneMode(toneMode: ToneMode, unitSystem: UnitSystem): Int = when (toneMode) {
    ToneMode.HorizontalSpeed,
    ToneMode.VerticalSpeed,
    ToneMode.TotalSpeed -> {
        this.fromSpeedUnitToCmPerSec(unitSystem)
    }
    ToneMode.GlideRatio,
    ToneMode.InverseGlideRatio,
    ToneMode.DiveAngle -> {
        this
    }
}

@Composable
fun rateMinimumLabel(rateMode: RateMode, unitSystem: UnitSystem): String = when (rateMode) {
    RateMode.HorizontalSpeed -> {
        stringResource(R.string.label_minimum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.VerticalSpeed -> {
        stringResource(R.string.label_minimum_vertical_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.GlideRatio -> {
        stringResource(R.string.label_minimum_glide_ratio)
    }

    RateMode.InverseGlideRatio -> {
        stringResource(R.string.label_minimum_inverse_glide_ratio)
    }

    RateMode.TotalSpeed -> {
        stringResource(R.string.label_minimum_total_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.MagnitudeOf1 -> {
        stringResource(R.string.label_minimum_magnitude_of_1)
    }

    RateMode.ChangeInValue1 -> {
        stringResource(R.string.label_minimum_change_in_value_1)
    }

    RateMode.DiveAngle -> {
        stringResource(R.string.label_minimum_dive_angle)
    }
}

@Composable
fun rateMaximumLabel(rateMode: RateMode, unitSystem: UnitSystem): String = when (rateMode) {
    RateMode.HorizontalSpeed -> {
        stringResource(R.string.label_maximum_horizontal_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.VerticalSpeed -> {
        stringResource(R.string.label_maximum_vertical_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.GlideRatio -> {
        stringResource(R.string.label_maximum_glide_ratio)
    }

    RateMode.InverseGlideRatio -> {
        stringResource(R.string.label_maximum_inverse_glide_ratio)
    }

    RateMode.TotalSpeed -> {
        stringResource(R.string.label_maximum_total_speed, stringResource(unitSystem.speedTextResource))
    }

    RateMode.MagnitudeOf1 -> {
        stringResource(R.string.label_maximum_magnitude_of_1)
    }

    RateMode.ChangeInValue1 -> {
        stringResource(R.string.label_maximum_change_in_value_1)
    }

    RateMode.DiveAngle -> {
        stringResource(R.string.label_maximum_dive_angle)
    }
}

fun Int.valueForRateMode(rateMode: RateMode, unitSystem: UnitSystem): Int = when (rateMode) {
    RateMode.HorizontalSpeed,
    RateMode.VerticalSpeed,
    RateMode.TotalSpeed -> {
        this.speedInUnit(unitSystem)
    }
    RateMode.GlideRatio,
    RateMode.InverseGlideRatio,
    RateMode.MagnitudeOf1,
    RateMode.ChangeInValue1,
    RateMode.DiveAngle -> {
        this
    }
}

fun Int.valueFromRateMode(rateMode: RateMode, unitSystem: UnitSystem): Int = when (rateMode) {
    RateMode.HorizontalSpeed,
    RateMode.VerticalSpeed,
    RateMode.TotalSpeed -> {
        this.fromSpeedUnitToCmPerSec(unitSystem)
    }
    RateMode.GlideRatio,
    RateMode.InverseGlideRatio,
    RateMode.MagnitudeOf1,
    RateMode.ChangeInValue1,
    RateMode.DiveAngle -> {
        this
    }
}

@Composable
fun speechValueLabel(speechMode: SpeechMode, unitSystem: UnitSystem): String = when (speechMode) {
    SpeechMode.AltitudeAboveDropzone -> {
        stringResource(R.string.speech_value_label_altitude_step, stringResource(unitSystem.distanceTextResource))
    }

    else -> {
        stringResource(R.string.misc_decimals)
    }
}

fun Int.speechValueForMode(speechMode: SpeechMode, unitSystem: UnitSystem): Int = when (speechMode) {
    SpeechMode.AltitudeAboveDropzone -> {
        this.distanceInUnit(unitSystem)
    }

    else -> {
        this
    }
}

fun Int.speechValueFromMode(speechMode: SpeechMode, unitSystem: UnitSystem): Int = when (speechMode) {
    SpeechMode.AltitudeAboveDropzone -> {
        this.fromDistanceUnitToMeter(unitSystem)
    }

    else -> {
        this
    }
}