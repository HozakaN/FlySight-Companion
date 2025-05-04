package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

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
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint

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
        onCreateReferencePointClicked = { viewModel.onCreateReferencePointClicked() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferencePointListScreenInternal(
    state: ReferencePointListState,
    onReferencePointClicked: (ReferencePoint) -> Unit,
    onReferencePointDelete: (ReferencePoint) -> Unit,
    onCreateReferencePointClicked: () -> Unit
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
            val sheetState = rememberModalBottomSheetState { sheetValue ->
                sheetValue != SheetValue.Hidden
            }
            LaunchedEffect(Unit) {
                sheetState.partialExpand()
            }

            ModalBottomSheet(
                onDismissRequest = {},
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
                            isSelectable = state.areReferencePointsSelectable,
                            onClick = { onReferencePointClicked(it) },
                            onDelete = { onReferencePointDelete(it) }
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = onCreateReferencePointClicked
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = "Add reference point"
                )
            }
        }
    }
}

@Composable
fun ReferencePointListItem(
    referencePoint: ReferencePoint,
    isSelectable: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    if (isSelectable) {
        Card(
            onClick = onClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(80.dp)
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
    } else {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
}