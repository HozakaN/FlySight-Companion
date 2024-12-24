package fr.hozakan.flysightcompanion.framework.compose

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider
import fr.hozakan.flysightcompanion.framework.menu.ActionBarMenuState

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> { noLocalProvidedFor("LocalViewModelFactory") }
val LocalMenuState = staticCompositionLocalOf<ActionBarMenuState> { noLocalProvidedFor("LocalMenuState") }

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}