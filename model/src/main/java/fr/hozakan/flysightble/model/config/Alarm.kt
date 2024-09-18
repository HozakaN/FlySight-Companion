package fr.hozakan.flysightble.model.config

data class Alarm(
    val alarmType: AlarmType,
    val alarmElevation: Int,
    val alarmFile: String
)