package fr.hozakan.flysightcompanion.externaldisplaymodule

import fr.hozakan.flysightcompanion.model.display.Display
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultExternalDisplayService : ExternalDisplayService {

    private val _displays = MutableStateFlow(emptyList<Display>())
    override val displays: StateFlow<List<Display>> = _displays.asStateFlow()
}