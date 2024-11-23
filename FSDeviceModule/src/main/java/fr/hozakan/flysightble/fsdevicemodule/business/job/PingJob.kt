package fr.hozakan.flysightble.fsdevicemodule.business.job

interface PingJob {
    suspend fun ping(timeout: Long = -1L): Boolean
}