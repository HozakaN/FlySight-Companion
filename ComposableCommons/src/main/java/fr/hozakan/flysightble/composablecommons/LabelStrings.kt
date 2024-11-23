package fr.hozakan.flysightble.composablecommons

import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.UnitSystem


fun toneMinimumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.VerticalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.GlideRatio -> {
        "Minimum glide ratio"
    }

    ToneMode.InverseGlideRatio -> {
        "Minimum glide ratio"
    }

    ToneMode.TotalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.DiveAngle -> {
        "Minimum angle (degrees)"
    }
}

fun toneMaximumLabel(toneMode: ToneMode, unitSystem: UnitSystem): String = when (toneMode) {
    ToneMode.HorizontalSpeed -> {
        "Maximum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.VerticalSpeed -> {
        "Maximum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.GlideRatio -> {
        "Maximum glide ratio"
    }

    ToneMode.InverseGlideRatio -> {
        "Maximum glide ratio"
    }

    ToneMode.TotalSpeed -> {
        "Maximum speed (${hourSpeed(unitSystem)})"
    }

    ToneMode.DiveAngle -> {
        "Maximum angle (degrees)"
    }
}

fun rateMinimumLabel(rateMode: RateMode, unitSystem: UnitSystem): String = when (rateMode) {
    RateMode.HorizontalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
    }

    RateMode.VerticalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
    }

    RateMode.GlideRatio -> {
        "Minimum glide ratio"
    }

    RateMode.InverseGlideRatio -> {
        "Minimum glide ratio"
    }

    RateMode.TotalSpeed -> {
        "Minimum speed (${hourSpeed(unitSystem)})"
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
        "Maximum speed (${hourSpeed(unitSystem)})"
    }

    RateMode.VerticalSpeed -> {
        "Maximum speed (${hourSpeed(unitSystem)})"
    }

    RateMode.GlideRatio -> {
        "Maximum glide ratio"
    }

    RateMode.InverseGlideRatio -> {
        "Maximum glide ratio"
    }

    RateMode.TotalSpeed -> {
        "Maximum speed (${hourSpeed(unitSystem)})"
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

fun hourSpeed(unitSystem: UnitSystem): String = when (unitSystem) {
    UnitSystem.Metric -> "km/h"
    UnitSystem.Imperial -> "mph"
}

fun distanceUnit(unitSystem: UnitSystem): String = when (unitSystem) {
    UnitSystem.Metric -> "m"
    UnitSystem.Imperial -> "ft"
}