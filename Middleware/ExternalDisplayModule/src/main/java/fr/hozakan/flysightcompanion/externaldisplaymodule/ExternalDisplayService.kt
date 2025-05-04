package fr.hozakan.flysightcompanion.externaldisplaymodule

import fr.hozakan.flysightcompanion.model.display.Display
import kotlinx.coroutines.flow.StateFlow

interface ExternalDisplayService {
    val displays: StateFlow<List<Display>>
}