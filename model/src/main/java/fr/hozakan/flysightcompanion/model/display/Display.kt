package fr.hozakan.flysightcompanion.model.display

data class Display(
    val id: Int,
    val name: String,
    val internal: android.view.Display
)
