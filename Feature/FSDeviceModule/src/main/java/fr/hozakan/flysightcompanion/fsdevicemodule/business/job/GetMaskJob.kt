package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface GetMaskJob {
    suspend fun getMask(timeout: Long = -1L): UByte
}