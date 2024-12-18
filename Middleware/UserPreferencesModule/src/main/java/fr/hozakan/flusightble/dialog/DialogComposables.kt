package fr.hozakan.flusightble.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

val LocalDialogService = staticCompositionLocalOf<MutableDialogService> {
    error("No DialogService provided")
}

@Composable
fun DialogHandler() {

    val dialogService = LocalDialogService.current

    var dialog by remember { mutableStateOf<Pair<DialogItem, Continuation<DialogResult>>?>(null) }

    LaunchedEffect(Unit) {
        dialogService.registerDialogDisplayer { dialogItem ->
            suspendCancellableCoroutine { continuation ->
                dialog = dialogItem to continuation
                continuation.invokeOnCancellation {
                    dialog = null
                }
            }
        }
    }

    dialog?.let { dialItem ->
        when (val dial = dialItem.first) {
            is ConfigFileNameDialog -> {
                dial.Content { result ->
                    dialog = null
                    dialItem.second.resume(result)
                }
            }
            is PickConfigurationDialog -> {
                dial.Content { result ->
                    dialog = null
                    dialItem.second.resume(result)
                }
            }
            else -> {}
        }
    }

}