package fr.hozakan.flysightcompanion.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
import kotlinx.coroutines.flow.combine

@Composable
fun DevScreen(
    usbService: UsbService,
    fsDeviceService: FsDeviceService,
    loggerService: LoggerService,
    audioService: AudioService,
    displayService: DisplayService,
    onBackClicked: () -> Unit
) {

    BackHandler { onBackClicked() }

    val logs by combine(
        usbService.usbLogs,
        fsDeviceService.logs,
        loggerService.logs
    ) { usbLogs, fsLogs, logs ->
        (usbLogs + fsLogs + logs).sortedBy { it.time }
    }.collectAsState(initial = emptyList())

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues)
        ) {
            var locked by remember { mutableStateOf(true) }
//            LockContainer(
//                locked = locked,
//                onUnlocked = {
//                    locked = !locked
//                },
//            )
//            Column {
//                Button(
//                    onClick = {
//                        audioService.playBeep(50)
//                    }
//                ) {
//                    Text("beep")
//                }
//                Button(
//                    onClick = {
//                        audioService.chirpUp(20)
//                    }
//                ) {
//                    Text("Chirp up")
//                }
//                Button(
//                    onClick = {
//                        audioService.chirpDown(10)
//                    }
//                ) {
//                    Text("Chirp down")
//                }
//                Button(
//                    onClick = {
//                        audioService.playFile("distance")
//                    }
//                ) {
//                    Text("Play file")
//                }
//                val context = LocalContext.current
//                Button(
//                    onClick = {
//                        audioService.playText("zero", 8, context.resources.configuration.locales[0])
//                    }
//                ) {
//                    Text("Play speech")
//                }
//                Spacer(modifier = Modifier.requiredHeight(16.dp))
//
//                val displays by displayService.displays.collectAsState()
//                LazyColumn {
//                    itemsIndexed(displays) { index, display ->
//                        FText(
//                            text = "Display #${index + 1} : ${display.name}",
//                            configuration = FlySightTheme.typography.plainScreenTextLarge,
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.requiredHeight(16.dp))
//                FText(
//                    modifier = Modifier.fillMaxWidth(),
//                    text = "Global logs",
//                    configuration = FlySightTheme.typography.cardTitle,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.requiredHeight(32.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        FText(
                            text = log.message,
                            configuration = FlySightTheme.typography.plainScreenTextLarge,
                        )
                    }
                }
//            }
        }
    }

}