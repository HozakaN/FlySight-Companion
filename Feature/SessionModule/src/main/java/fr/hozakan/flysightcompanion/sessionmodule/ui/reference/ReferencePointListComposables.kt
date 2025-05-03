package fr.hozakan.flysightcompanion.sessionmodule.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
        onReferencePointDelete = { viewModel.onReferencePointDelete(it) }
    )
}

@Composable
fun ReferencePointListScreenInternal(
    state: ReferencePointListState,
    onReferencePointClicked: (ReferencePoint) -> Unit = {},
    onReferencePointDelete: (ReferencePoint) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
            Column {
                FText(
                    text = "Latitude : ${referencePoint.coords.latitude}"
                )
                FText(
                    text = "Latitude : ${referencePoint.coords.latitude}"
                )
                FText(
                    text = "Longitude : ${referencePoint.coords.longitude}"
                )
            }
        }
    } else {
        Card {
            Column {
                FText(
                    text = "Latitude : ${referencePoint.coords.latitude}"
                )
                FText(
                    text = "Latitude : ${referencePoint.coords.latitude}"
                )
                FText(
                    text = "Longitude : ${referencePoint.coords.longitude}"
                )
            }
        }
    }
}