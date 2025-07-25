package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import kotlinx.coroutines.launch

@Composable
fun ReferencePointListMenuActions(
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
fun ReferencePointListScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: ReferencePointListViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    ReferencePointListScreenInternal(
        state = state,
        onReferencePointClicked = { viewModel.onReferencePointClicked(it) },
        onReferencePointDelete = { viewModel.onReferencePointDelete(it) },
        onCreateReferencePointClicked = { viewModel.onCreateReferencePointClicked() },
        onMapLongClick = { latitude, longitude -> viewModel.onMapLongClick(latitude, longitude) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferencePointListScreenInternal(
    state: ReferencePointListState,
    onReferencePointClicked: (ReferencePoint) -> Unit,
    onReferencePointDelete: (ReferencePoint) -> Unit,
    onCreateReferencePointClicked: () -> Unit,
    onMapLongClick: (Double, Double) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {

            val selectedReferencePoint = state.selectedReferencePoint

            val cameraPositionState = rememberCameraPositionState()

            LaunchedEffect(selectedReferencePoint) {
                if (selectedReferencePoint != null) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                LatLng(
                                    selectedReferencePoint.coords.latitude,
                                    selectedReferencePoint.coords.longitude
                                ),
                                15f
                            )
                        ),
                        durationMs = 1000
                    )
                }
            }

            GoogleMap(
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    onMapLongClick(latLng.latitude, latLng.longitude)
                }
            ) {
                state.referencePoints.forEach { refPoint ->
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(refPoint.coords.latitude, refPoint.coords.longitude)
                        ),
                        title = refPoint.name,
                    )
                }
            }

            val sheetState = rememberModalBottomSheetState()

            LaunchedEffect(Unit) {
                sheetState.partialExpand()
            }
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            sheetState.partialExpand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Open reference point list sheet"
                    )
                }
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                FloatingActionButton(
                    onClick = onCreateReferencePointClicked
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = "Add reference point"
                    )
                }
            }
            if (sheetState.currentValue != SheetValue.Hidden || sheetState.targetValue != SheetValue.Hidden) {
                ModalBottomSheet(
                    onDismissRequest = {
                        scope.launch {
                            sheetState.hide()
                        }
                    },
                    sheetState = sheetState,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.referencePoints) {
                            ReferencePointListItem(
                                referencePoint = it,
                                isSelected = it == selectedReferencePoint,
                                onClick = {
                                    onReferencePointClicked(it)
                                    scope.launch {
                                        sheetState.hide()
                                    }
                                },
                                onDelete = { onReferencePointDelete(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferencePointListItem(
    referencePoint: ReferencePoint,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = {
            if (!isSelected) {
                onClick()
            }
        },
        border = if (isSelected) {
            BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(120.dp)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    FText(
                        text = referencePoint.name,
                        configuration = FlySightTheme.typography.cardTitle
                    )
                    FText(
                        text = "Latitude : ${referencePoint.coords.latitude}"
                    )
                    FText(
                        text = "Longitude : ${referencePoint.coords.longitude}"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .requiredSize(48.dp)
                        .clickable {
                            onDelete()
                        }
                        .padding(8.dp),
                    imageVector = Icons.Default.Delete,
                    contentDescription = ""
                )
            }
        }
    }
}