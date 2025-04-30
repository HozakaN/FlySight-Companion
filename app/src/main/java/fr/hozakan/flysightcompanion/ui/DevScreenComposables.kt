package fr.hozakan.flysightcompanion.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
import kotlinx.coroutines.flow.combine

@Composable
fun DevScreen(
    usbService: UsbService,
    fsDeviceService: FsDeviceService,
    loggerService: LoggerService,
    onBackClicked: () -> Unit
) {

    BackHandler { onBackClicked() }

    val logs by combine(usbService.usbLogs, fsDeviceService.logs, loggerService.logs) { usbLogs, fsLogs, logs ->
        (usbLogs + fsLogs + logs).sortedBy { it.time }
    }.collectAsState(initial = emptyList())

    Surface {
        Column {
            FText(
                modifier = Modifier.fillMaxWidth(),
                text = "Global logs",
                configuration = FlySightTheme.typography.cardTitle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.requiredHeight(32.dp))
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
        }
    }

}