package fr.hozakan.flysightcompanion.designsystem.extension

import android.content.Context
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.StaticSessionSource

val UnitSystem.unitNameResource: Int
    get() = when (this) {
        UnitSystem.Metric -> R.string.misc_unit_name_metric
        UnitSystem.Imperial -> R.string.misc_unit_name_imperial
    }

val UnitSystem.speedTextResource: Int
    get() = when (this) {
        UnitSystem.Metric -> R.string.misc_speed_text_metric
        UnitSystem.Imperial -> R.string.misc_speed_text_imperial
    }

val UnitSystem.distanceTextResource: Int
    get() = when (this) {
        UnitSystem.Metric -> R.string.misc_distance_text_metric
        UnitSystem.Imperial -> R.string.misc_distance_text_imperial
    }

val DynamicModel.textResource: Int
    get() = when (this) {
        DynamicModel.Portable -> R.string.dynamic_model_text_portable
        DynamicModel.Stationary -> R.string.dynamic_model_text_stationary
        DynamicModel.Pedestrian -> R.string.dynamic_model_text_pedestrian
        DynamicModel.Automotive -> R.string.dynamic_model_text_automotive
        DynamicModel.Sea -> R.string.dynamic_model_text_sea
        DynamicModel.Airborne1g -> R.string.dynamic_model_text_airborne_with_1G_acceleration
        DynamicModel.Airborne2g -> R.string.dynamic_model_text_airborne_with_2G_acceleration
        DynamicModel.Airborne4g -> R.string.dynamic_model_text_airborne_with_4G_acceleration
    }

fun DynamicModel.Companion.fromText(context: Context, text: String): DynamicModel? =
    DynamicModel.entries.firstOrNull { context.getString(it.textResource) == text }

val AlarmType.textResource: Int
    get() = when (this) {
        AlarmType.NoAlarm -> R.string.alarm_type_no_alarm
        AlarmType.Beep -> R.string.alarm_type_beep
        AlarmType.ChirpUp -> R.string.alarm_type_chirp_up
        AlarmType.ChirpDown -> R.string.alarm_type_chirp_down
        AlarmType.PlayFile -> R.string.alarm_type_clay_file
    }

fun AlarmType.Companion.fromText(context: Context, text: String): AlarmType? =
    AlarmType.entries.firstOrNull { context.getString(it.textResource) == text }

val InitMode.textResource: Int
    get() = when (this) {
        InitMode.DoNothing -> R.string.init_mode_do_nothing
        InitMode.TestSpeechMode -> R.string.init_mode_test_speech_mode
        InitMode.PlayFile -> R.string.init_mode_play_file
    }

fun InitMode.Companion.fromText(context: Context, text: String): InitMode? =
    InitMode.entries.firstOrNull { context.getString(it.textResource) == text }

val RateMode.textResource: Int
    get() = when (this) {
        RateMode.HorizontalSpeed -> R.string.rate_mode_horizontal_speed
        RateMode.VerticalSpeed -> R.string.rate_mode_vertical_speed
        RateMode.GlideRatio -> R.string.rate_mode_glide_ratio
        RateMode.InverseGlideRatio -> R.string.rate_mode_inverse_glide_ratio
        RateMode.TotalSpeed -> R.string.rate_mode_total_speed
        RateMode.MagnitudeOf1 -> R.string.rate_mode_magnitude_of_tone_value
        RateMode.ChangeInValue1 -> R.string.rate_mode_change_in_tone_value
        RateMode.DiveAngle -> R.string.rate_mode_dive_angle
    }

fun RateMode.Companion.fromText(context: Context, text: String): RateMode? =
    RateMode.entries.firstOrNull { context.getString(it.textResource) == text }

val SpeechMode.textResource: Int
    get() = when (this) {
        SpeechMode.HorizontalSpeed -> R.string.speech_mode_horizontal_speed
        SpeechMode.VerticalSpeed -> R.string.speech_mode_vertical_speed
        SpeechMode.GlideRatio -> R.string.speech_mode_glide_ratio
        SpeechMode.InverseGlideRatio -> R.string.speech_mode_inverse_glide_ratio
        SpeechMode.TotalSpeed -> R.string.speech_mode_total_speed
        SpeechMode.AltitudeAboveDropzone -> R.string.speech_mode_altitude_above_dropzone
        SpeechMode.DiveAngle -> R.string.speech_mode_dive_angle
    }

fun SpeechMode.Companion.fromText(context: Context, text: String): SpeechMode? =
    SpeechMode.entries.firstOrNull { context.getString(it.textResource) == text }

val ToneLimitBehaviour.textResource: Int
    get() = when (this) {
        ToneLimitBehaviour.NoTone -> R.string.tone_limit_behaviour_no_tone
        ToneLimitBehaviour.MinMaxTone -> R.string.tone_limit_behaviour_min_max_tone
        ToneLimitBehaviour.ChirpUpDown -> R.string.tone_limit_behaviour_chirp_up_down
        ToneLimitBehaviour.ChirpDownUp -> R.string.tone_limit_behaviour_chirp_down_up
    }

fun ToneLimitBehaviour.Companion.fromText(context: Context, text: String): ToneLimitBehaviour? {
    return ToneLimitBehaviour.entries.firstOrNull { context.getString(it.textResource) == text }
}

val ToneMode.textResource: Int
    get() = when (this) {
        ToneMode.HorizontalSpeed -> R.string.tone_mode_horizontal_speed
        ToneMode.VerticalSpeed -> R.string.tone_mode_vertical_speed
        ToneMode.GlideRatio -> R.string.tone_mode_glide_ratio
        ToneMode.InverseGlideRatio -> R.string.tone_mode_inverse_glide_ratio
        ToneMode.TotalSpeed -> R.string.tone_mode_total_speed
        ToneMode.DiveAngle -> R.string.tone_mode_dive_angle
    }

fun ToneMode.Companion.fromText(context: Context, text: String): ToneMode? {
    return ToneMode.entries.firstOrNull { context.getString(it.textResource) == text }
}

val StaticSessionSource.textResource: Int
    get() = when (this) {
        StaticSessionSource.Local -> R.string.session_configuration_source_local
        StaticSessionSource.FlySight -> R.string.session_configuration_source_flysight
        StaticSessionSource.File -> R.string.session_configuration_source_file
    }

fun StaticSessionSource.Companion.fromText(context: Context, text: String): StaticSessionSource? {
    return StaticSessionSource.entries.firstOrNull { context.getString(it.textResource) == text }
}
