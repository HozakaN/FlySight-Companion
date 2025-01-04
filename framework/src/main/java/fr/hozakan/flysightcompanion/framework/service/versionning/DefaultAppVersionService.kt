package fr.hozakan.flysightcompanion.framework.service.versionning

class DefaultAppVersionService(
    override val appVersion: String,
    override val appCode: Int
) : AppVersionService