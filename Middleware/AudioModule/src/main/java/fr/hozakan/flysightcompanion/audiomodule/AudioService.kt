package fr.hozakan.flysightcompanion.audiomodule

import java.util.Locale

interface AudioService {
    fun playBeep(volume: Int)
    fun chirpUp(volume: Int)
    fun chirpDown(volume: Int)
    fun playFile(fileName: String)
    fun playText(speech: String, volume: Int, locale: Locale)
}