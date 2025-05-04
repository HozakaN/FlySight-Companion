package fr.hozakan.flysightcompanion.sessionmodule.business.player

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.Window
import androidx.compose.ui.platform.ComposeView
import fr.hozakan.flysightcompanion.sessionmodule.ui.play.SessionPlayerScreen

class VideoPresentation(
    context: Context,
    display: Display
) : Presentation(context, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(
            ComposeView(context).apply {
                setContent {
                    SessionPlayerScreen()
                }
            }
        )
    }

}