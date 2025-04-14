package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

import fr.hozakan.flysightcompanion.model.DeviceMode

interface SetModeJob {
    suspend fun setMode(
        mode: DeviceMode,
        timeout: Long = -1L
    ): Boolean
}