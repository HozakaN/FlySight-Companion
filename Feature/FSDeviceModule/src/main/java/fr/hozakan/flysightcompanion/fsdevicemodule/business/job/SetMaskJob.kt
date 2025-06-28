package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

interface SetMaskJob {
    suspend fun setMask(
        mask: UByte,
        timeout: Long = -1L
    ): Boolean
}