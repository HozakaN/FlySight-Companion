package fr.hozakan.flysightcompanion.dialogmodule

class DefaultDialogService : MutableDialogService {

    private var dialogDisplayer: (suspend (DialogItem) -> DialogResult)? = null

    override suspend fun registerDialogDisplayer(dialogDisplayer: suspend (DialogItem) -> DialogResult) {
        this.dialogDisplayer = dialogDisplayer
    }

    override fun unregisterDialogDisplayer(dialogDisplayer: suspend (DialogItem) -> DialogResult) {
        if (this.dialogDisplayer == dialogDisplayer)  {
            this.dialogDisplayer = null
        }
    }

    override suspend fun displayDialog(dialogItem: DialogItem): DialogResult =
        dialogDisplayer?.invoke(dialogItem) ?: throw IllegalStateException("No dialog displayer registered")
}