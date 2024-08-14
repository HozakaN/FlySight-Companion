package fr.hozakan.flysightble.framework.compose

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> { noLocalProvidedFor("LocalViewModelFactory") }

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}