package fr.hozakan.flysightcompanion.model.firmware

import com.google.gson.annotations.SerializedName

data class FirmwareCompatibilityMatrix(
    @SerializedName("desc")
    val description: String,
    val firmwares: List<FirmwareInfo>,
    val apps: List<AppVersionInfo>,
    @SerializedName("batch_infos")
    val batchInfos: List<BatchInfo>
) {

    fun getFirmwareInfoByName(name: String): FirmwareInfo? =
        firmwares.firstOrNull { it.name == name }

    companion object {
        val placeholder = FirmwareCompatibilityMatrix("", emptyList(), emptyList(), emptyList())
    }
}

data class BatchInfo(
    val key: String,
    @SerializedName("batch_prefix")
    val batchPrefix: String
)

data class AppVersionInfo(
    val name: String,
    @SerializedName("firmware_compatibility")
    val firmwareCompatibility: List<String>
)

data class FirmwareInfo(
    val name: String,
    @SerializedName("app_compatibility")
    val appCompatibility: List<String>
) {
    val isBeta: Boolean
        get() = name.contains("beta", ignoreCase = true) || name.contains(
            "develop",
            ignoreCase = true
        )

    val hasGnssMaskCommand: Boolean
        get() = name !in gnssMaskCommandBlackList
}

private val gnssMaskCommandBlackList = listOf(
    "v2024.12.30",
    "v2024.11.11.release_candidate"
)