package fr.hozakan.flysightcompanion.model.firmware

import com.google.gson.annotations.SerializedName

data class FirmwareCompatibilityMatrix(
    val description: String,
    val firmwares: List<FirmwareInfo>,
    val apps: List<AppVersionInfo>,
    @SerializedName("batch_infos")
    val batchInfos: List<BatchInfo>
) {
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
)