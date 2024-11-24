package fr.hozakan.flysightble.composablecommons

import fr.hozakan.flysightble.framework.extension.distanceInUnit
import fr.hozakan.flysightble.framework.extension.fromDistanceUnitToMeter
import fr.hozakan.flysightble.framework.extension.fromSpeedUnitToCmPerSec
import fr.hozakan.flysightble.framework.extension.speedInUnit
import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.SpeechMode
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.UnitSystem


fun toneMinimumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    ToneMode.VerticalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    ToneMode.GlideRatio -> {
        "Minimum glide ratio"
    }

    ToneMode.InverseGlideRatio -> {
        "Minimum glide ratio"
    }

    ToneMode.TotalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    ToneMode.DiveAngle -> {
        "Minimum angle (degrees)"
    }
}

fun toneMaximumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    ToneMode.VerticalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    ToneMode.GlideRatio -> {
        "Maximum glide ratio"
    }

    ToneMode.InverseGlideRatio -> {
        "Maximum glide ratio"
    }

    ToneMode.TotalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    ToneMode.DiveAngle -> {
        "Maximum angle (degrees)"
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

fun rateMinimumLabel(rateMode: RateMode, unitSystem: UnitSystem): String = when (rateMode) {
    RateMode.HorizontalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    RateMode.VerticalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    RateMode.GlideRatio -> {
        "Minimum glide ratio"
    }

    RateMode.InverseGlideRatio -> {
        "Minimum glide ratio"
    }

    RateMode.TotalSpeed -> {
        "Minimum speed (${unitSystem.speedText})"
    }

    RateMode.MagnitudeOf1 -> {
        "Minimum magnitude"
    }

    RateMode.ChangeInValue1 -> {
        "Minimum change (percent/s)"
    }

    RateMode.DiveAngle -> {
        "Minimum angle (degrees)"
    }
}

fun rateMaximumLabel(rateMode: RateMode, unitSystem: UnitSystem): String = when (rateMode) {
    RateMode.HorizontalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    RateMode.VerticalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    RateMode.GlideRatio -> {
        "Maximum glide ratio"
    }

    RateMode.InverseGlideRatio -> {
        "Maximum glide ratio"
    }

    RateMode.TotalSpeed -> {
        "Maximum speed (${unitSystem.speedText})"
    }

    RateMode.MagnitudeOf1 -> {
        "Maximum magnitude"
    }

    RateMode.ChangeInValue1 -> {
        "Maximum change (percent/s)"
    }

    RateMode.DiveAngle -> {
        "Maximum angle (degrees)"
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

fun speechValueLabel(speechMode: SpeechMode, unitSystem: UnitSystem): String = when (speechMode) {
    SpeechMode.AltitudeAboveDropzone -> {
        "Altitude step (${unitSystem.distanceText})"
    }

    else -> {
        "Decimals"
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