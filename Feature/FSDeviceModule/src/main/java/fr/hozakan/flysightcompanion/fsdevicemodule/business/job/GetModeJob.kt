package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

import fr.hozakan.flysightcompanion.model.DeviceMode

interface GetModeJob {
    suspend fun getMode(timeout: Long = -1L): DeviceMode
}