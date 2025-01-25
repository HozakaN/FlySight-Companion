package fr.hozakan.flysightcompanion.model.firmware

import com.google.gson.annotations.SerializedName

data class FirmwareCompatibilityMatrix(
    val description: String,
    val firmwares: List<FirmwareInfo>,
    val apps: List<AppVersionInfo>
) {
    companion object {
        val placeholder = FirmwareCompatibilityMatrix("", emptyList(), emptyList())
    }
}

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