package fr.hozakan.flysightcompanion.externaldisplaymodule

import fr.hozakan.flysightcompanion.model.display.Display
import kotlinx.coroutines.flow.StateFlow

interface DisplayService {
    val displays: StateFlow<List<Display>>
    fun lockDisplay(lock: Boolean)
}