package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface PingJob {
    suspend fun ping(timeout: Long = -1L): Boolean
}