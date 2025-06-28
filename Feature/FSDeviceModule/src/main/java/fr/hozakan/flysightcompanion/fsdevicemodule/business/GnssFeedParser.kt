package fr.hozakan.flysightcompanion.fsdevicemodule.business

import fr.hozakan.flysightcompanion.model.GnssData

interface GnssFeedParser {
    fun parse(value: ByteArray): GnssData?
}