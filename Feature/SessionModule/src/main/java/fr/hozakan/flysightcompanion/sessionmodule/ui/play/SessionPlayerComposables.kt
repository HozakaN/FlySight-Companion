package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItem
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.ui.SpeedOrientation
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SessionPlayerMenuActions(
//    onCreatePlayFile: () -> Unit
) {
    IconButton(
        onClick = {} //onCreatePlayFile
    ) {
//        Icon(
//            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
//            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
//        )
    }
}

@Composable
fun SessionPlayerScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionPlayerViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface
        ) {
            SessionPlayerScreenInternal(
                state = state
            )
        }
    }
}

@Composable
private fun SessionPlayerScreenInternal(state: SessionPlayerState) {
    val player = state.player
    if (player == null) return
    val displayGrid = player.profile.displayGrid
    when (displayGrid) {
        DisplayGrid.InlineLeft -> InlineLeftPlayerScreen(player = player)
        DisplayGrid.InlineRight -> InlineLeftPlayerScreen(player = player)
        DisplayGrid.TwoByTwo -> InlineLeftPlayerScreen(player = player)
        DisplayGrid.TwoOnEachSide -> InlineLeftPlayerScreen(player = player)
        DisplayGrid.ThreeOnEachSide -> ThreeOnEachSidePlayerScreen(player = player)
    }
}

@Composable
private fun ThreeOnEachSidePlayerScreen(
    player: SessionController
) {
    val displayItems = player.profile.displayItems
    Row {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(5f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Center Container")
            }
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 5 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 5 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 5 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayCapabilityContainer(
    item: DisplayItem,
    config: ConfigFile,
    player: SessionController
) {
    val gnssData: GnssData? by player.gnssFlow.collectAsState(initial = null)
    when (item.displayableCapability) {
        DisplayableCapability.HorizontalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Horizontal,
            player = player
        )

        DisplayableCapability.VerticalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Vertical,
            player = player
        )

        DisplayableCapability.TotalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Total,
            player = player
        )

        DisplayableCapability.Elevation -> TagAndValueContainer(
            tag = "Elv",
            value = "${gnssData?.hMsl?.minus(config.dzElev)}"
        )

        DisplayableCapability.Altitude -> TagAndValueContainer(
            tag = "Alt",
            value = "${gnssData?.hMsl}"
        )

        DisplayableCapability.DistanceToReferencePoint -> TagAndValueContainer(
            tag = "RefPt",
            value = "1425 m"
        )

        DisplayableCapability.Latitude -> TagAndValueContainer(
            tag = "Lat",
            value = "${gnssData?.lat}"
        )

        DisplayableCapability.Longitude -> TagAndValueContainer(
            tag = "Lon",
            value = "${gnssData?.lon}"
        )
    }
}

@Composable
private fun TagAndValueContainer(tag: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        Text(
            text = value
        )
    }
}

@Composable
private fun SpeedContainer(orientation: SpeedOrientation, player: SessionController) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "",
            modifier = Modifier.graphicsLayer {
                rotationZ = when (orientation) {
                    SpeedOrientation.Horizontal -> 0f
                    SpeedOrientation.Vertical -> 90f
                    SpeedOrientation.Total -> 45f
                }
            }
        )
        Text(
            text = "145 km/h"
        )
    }
}

@Composable
private fun InlineLeftPlayerScreen(player: SessionController) {
    val displayItems = player.profile.displayItems
    Row {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn {
                items(displayItems) { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = player.profile.configFile,
                        player = player
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Center Container")
        }
    }
}

@Preview(
    name = "Landscape Preview",
    widthDp = 640,
    heightDp = 360
)
@Composable
fun ThreeOnEachSidePlayerScreenPreview() {
    ThreeOnEachSidePlayerScreen(
        player = FakeSessionController(
            profile = fakeProfile
        )
    )
}

@Preview(
    name = "Landscape Preview",
    widthDp = 640,
    heightDp = 360
)
@Composable
fun InlineLeftPlayerScreenPreview() {
    InlineLeftPlayerScreen(
        player = FakeSessionController(
            profile = fakeProfile.copy(
                displayItems = listOf(
                    DisplayItem(
                        displayableCapability = DisplayableCapability.HorizontalSpeed,
                        caseIndex = 0,
                        indexInCase = 0
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.VerticalSpeed,
                        caseIndex = 0,
                        indexInCase = 1
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.TotalSpeed,
                        caseIndex = 0,
                        indexInCase = 2
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.Altitude,
                        caseIndex = 1,
                        indexInCase = 0
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.Elevation,
                        caseIndex = 1,
                        indexInCase = 1
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                        caseIndex = 1,
                        indexInCase = 2
                    )
                )
            )
        )
    )
}

class FakeSessionController(
    override val profile: SessionProfile
) : SessionController {
    override val sessionEvents: SharedFlow<SessionEvent> = MutableSharedFlow()

    override val navLane: StateFlow<LoadingState<Int>> = MutableStateFlow(LoadingState.Loading())
    override val gnssFlow: SharedFlow<GnssData> = MutableSharedFlow()

    override fun pause() {}

    override fun play() {}
    override fun play(callback: SessionController.SessionControllerCallback) {}

    override fun destroy() {}

}

private val fakeProfile = SessionProfile.default()
    .copy(
        displayItems = listOf(
            DisplayItem(
                displayableCapability = DisplayableCapability.HorizontalSpeed,
                caseIndex = 0,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.VerticalSpeed,
                caseIndex = 0,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.TotalSpeed,
                caseIndex = 0,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 1,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 1,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 1,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 2,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 2,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 2,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 3,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 3,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 3,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 4,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 4,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 4,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 5,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 5,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 5,
                indexInCase = 2
            ),
        )
    )