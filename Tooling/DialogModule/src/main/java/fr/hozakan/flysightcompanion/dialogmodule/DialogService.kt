package fr.hozakan.flysightcompanion.dialogmodule

import androidx.compose.runtime.Composable

sealed interface DialogItem {
    @Composable
    fun Content(onResult: (DialogResult) -> Unit)
}

sealed interface DialogResult {
    data object Dismiss : DialogResult
}

data object OkDialogResult : DialogResult


interface DialogService {
    suspend fun displayDialog(dialogItem: DialogItem): DialogResult
}

interface MutableDialogService : DialogService {
    suspend fun registerDialogDisplayer(dialogDisplayer: suspend (DialogItem) -> DialogResult)
    fun unregisterDialogDisplayer(dialogDisplayer: suspend (DialogItem) -> DialogResult)
}