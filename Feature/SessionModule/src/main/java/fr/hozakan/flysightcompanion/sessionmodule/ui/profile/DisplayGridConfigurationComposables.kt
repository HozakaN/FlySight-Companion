package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.model.session.profile.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItem
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.profile.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile

@Composable
fun DisplayGridConfigurationScreen(
    form: SessionProfileForm,
    referencePoints: List<ReferencePoint>,
    onDismiss: () -> Unit
) {

    BackHandler(enabled = true) {
        onDismiss()
    }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout),
        color = MaterialTheme.colorScheme.surface
    ) {

        var addItemClickedInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var addDstToRefPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }

        when (form.displayGrid) {
            DisplayGrid.InlineLeft -> InlineLayout(
                layoutDirection = InlineLayoutDirection.LEFT,
                displayItems = form.displayItems,
                onAddItemClicked = { caseIndex, indexInCase ->
                    addItemClickedInfo = caseIndex to indexInCase
                },
                onDeleteItemClicked = {
                    form.removeDisplayItem(it)
                }
            )

            DisplayGrid.InlineRight -> InlineLayout(
                layoutDirection = InlineLayoutDirection.RIGHT,
                displayItems = form.displayItems,
                onAddItemClicked = { caseIndex, indexInCase ->
                    addItemClickedInfo = caseIndex to indexInCase
                },
                onDeleteItemClicked = {
                    form.removeDisplayItem(it)
                }
            )

            DisplayGrid.TwoByTwo -> TwoByTwoLayout(
                displayItems = form.displayItems,
                onAddItemClicked = { caseIndex, indexInCase ->
                    addItemClickedInfo = caseIndex to indexInCase
                },
                onDeleteItemClicked = {
                    form.removeDisplayItem(it)
                }
            )

            DisplayGrid.TwoOnEachSide -> TwoOrThreeOnEachSideLayout(
                isThreeColumnLayout = false,
                displayItems = form.displayItems,
                onAddItemClicked = { caseIndex, indexInCase ->
                    addItemClickedInfo = caseIndex to indexInCase
                },
                onDeleteItemClicked = {
                    form.removeDisplayItem(it)
                }
            )

            DisplayGrid.ThreeOnEachSide -> TwoOrThreeOnEachSideLayout(
                isThreeColumnLayout = true,
                displayItems = form.displayItems,
                onAddItemClicked = { caseIndex, indexInCase ->
                    addItemClickedInfo = caseIndex to indexInCase
                },
                onDeleteItemClicked = {
                    form.removeDisplayItem(it)
                }
            )
        }

        if (addItemClickedInfo != null) {
            AddItemDialog(
                onDismissRequest = {
                    addItemClickedInfo = null
                },
                onItemPicked = { item ->
                    if (item == DisplayableCapability.DistanceToReferencePoint) {
                        addDstToRefPoint = addItemClickedInfo
                    } else {
                        form.addDisplayItem(
                            DisplayItem(
                                displayableCapability = item,
                                caseIndex = addItemClickedInfo!!.first,
                                indexInCase = addItemClickedInfo!!.second
                            )
                        )
                    }
                    addItemClickedInfo = null
                },
            )
        } else if (addDstToRefPoint != null) {
            PickReferencePointDialog(
                referencePoints = referencePoints,
                onDismissRequest = {
                    addDstToRefPoint = null
                },
                onItemPicked = { item ->
                    form.addDisplayItem(
                        DisplayItem(
                            displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                            caseIndex = addDstToRefPoint!!.first,
                            indexInCase = addDstToRefPoint!!.second,
                            bag = DisplayItemBundle.dstToRefPt(item)
                        )
                    )
                    addDstToRefPoint = null
                },
            )
        }
    }
}

@Composable
fun PickReferencePointDialog(
    referencePoints: List<ReferencePoint>,
    onDismissRequest: () -> Unit,
    onItemPicked: (ReferencePoint) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            LazyColumn {
                items(referencePoints) { referencePoint ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(36.dp)
                            .clickable {
                                onItemPicked(referencePoint)
                            }
                            .padding(8.dp),
                        text = referencePoint.name,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AddItemDialog(
    onDismissRequest: () -> Unit,
    onItemPicked: (DisplayableCapability) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            LazyColumn {
                items(DisplayableCapability.entries) { item ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(36.dp)
                            .clickable {
                                onItemPicked(item)
                            }
                            .padding(8.dp),
                        text = stringResource(item.textResource),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AddItemDialogPreview() {
    AddItemDialog(
        onDismissRequest = {},
        onItemPicked = {}
    )
}

enum class InlineLayoutDirection {
    LEFT,
    RIGHT
}

@Composable
private fun InlineLayout(
    layoutDirection: InlineLayoutDirection,
    displayItems: List<DisplayItem>,
    onAddItemClicked: (Int, Int) -> Unit,
    onDeleteItemClicked: (DisplayItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        if (layoutDirection == InlineLayoutDirection.LEFT) {
            InlineLayoutDisplayItems(displayItems, onDeleteItemClicked)
        }
        Column(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(4.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FText(
                text = "Main Content Area",
                configuration = FlySightTheme.typography.plainScreenTextLarge
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Button(
                onClick = { onAddItemClicked(0, displayItems.size) }
            ) {
                Text("Add item")
            }
        }

        if (layoutDirection == InlineLayoutDirection.RIGHT) {
            InlineLayoutDisplayItems(displayItems, onDeleteItemClicked)
        }
    }
}

@Composable
private fun RowScope.InlineLayoutDisplayItems(
    displayItems: List<DisplayItem>,
    onDeleteItemClicked: (DisplayItem) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(3f)
            .fillMaxHeight()
            .border(2.dp, Color.Green),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        displayItems.forEach { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FText(
                    text = stringResource(item.displayableCapability.textResource),
                    configuration = FlySightTheme.typography.sessionConfigurationText,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    onDeleteItemClicked(item)
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item"
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoByTwoLayout(
    displayItems: List<DisplayItem>,
    onAddItemClicked: (Int, Int) -> Unit,
    onDeleteItemClicked: (DisplayItem) -> Unit
) {

    val items = remember(displayItems) {
        displayItems.groupBy { item -> item.caseIndex }
            .map { itemGroup -> itemGroup.value.sortedBy { item -> item.indexInCase } }.let {
            if (it.size < 4) {
                val mutableList = it.toMutableList()
                (0..3).filter { index -> displayItems.none { displayItem -> displayItem.caseIndex == index } }.forEach { index ->
                    mutableList.add(index, emptyList())
                }
                mutableList
            } else {
                it
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top row (2 boxes)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Top left
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 0,
                    isThreeColumnLayout = false,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            // Top right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 2,
                    isThreeColumnLayout = false,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }
        }

        // Bottom row (2 boxes)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Bottom left
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 1,
                    isThreeColumnLayout = false,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            // Bottom right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 3,
                    isThreeColumnLayout = false,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }
        }
    }
}

@Composable
private fun GridDisplayItemsContainer(
    index: Int,
    isThreeColumnLayout: Boolean,
    items: List<List<DisplayItem>>,
    onDeleteItemClicked: (DisplayItem) -> Unit,
    onAddItemClicked: (Int, Int) -> Unit
) {
    val selectedItems = items.getOrNull(index)
    if (selectedItems != null) {
        DisplayItemsContainer(
            isThreeColumnLayout = isThreeColumnLayout,
            items = selectedItems,
            onDeleteItemClicked = onDeleteItemClicked,
            onAddItemClicked = {
                onAddItemClicked(index, selectedItems.size)
            }
        )
    } else {
        Button(
            modifier = Modifier
                .padding(8.dp),
            onClick = {
                onAddItemClicked(index, 0)
            }
        ) {
            FText(
                text = "Add item",
                configuration = FlySightTheme.typography.plainScreenTextLarge
            )
        }
    }
}

@Composable
private fun TwoOrThreeOnEachSideLayout(
    isThreeColumnLayout: Boolean,
    displayItems: List<DisplayItem>,
    onAddItemClicked: (Int, Int) -> Unit,
    onDeleteItemClicked: (DisplayItem) -> Unit
) {

    val items = remember(displayItems) {
        displayItems.groupBy { item -> item.caseIndex }
            .map { itemGroup -> itemGroup.value.sortedBy { item -> item.indexInCase } }.let {
                if (it.size < 4) {
                    val mutableList = it.toMutableList()
                    (0..5).filter { index -> displayItems.none { displayItem -> displayItem.caseIndex == index } }.forEach { index ->
                        mutableList.add(index, emptyList())
                    }
                    mutableList
                } else {
                    it
                }
            }
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left column with weight 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(width = 2.dp, color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 0,
                    isThreeColumnLayout = isThreeColumnLayout,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 1,
                    isThreeColumnLayout = isThreeColumnLayout,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            if (isThreeColumnLayout) {

                // Two centered boxes of the same size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(width = 2.dp, color = Color.Green),
                    contentAlignment = Alignment.Center
                ) {
                    GridDisplayItemsContainer(
                        index = 2,
                        isThreeColumnLayout = isThreeColumnLayout,
                        items,
                        onDeleteItemClicked,
                        onAddItemClicked
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .border(width = 2.dp, color = Color.Green)
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            FText(
                text = "Main Content",
                configuration = FlySightTheme.typography.plainScreenTextLarge
            )
        }

        // Right column with weight 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(width = 2.dp, color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 3,
                    isThreeColumnLayout = isThreeColumnLayout,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 2.dp, color = Color.Green),
                contentAlignment = Alignment.Center
            ) {
                GridDisplayItemsContainer(
                    index = 4,
                    isThreeColumnLayout = isThreeColumnLayout,
                    items,
                    onDeleteItemClicked,
                    onAddItemClicked
                )
            }

            if (isThreeColumnLayout) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(width = 2.dp, color = Color.Green),
                    contentAlignment = Alignment.Center
                ) {
                    GridDisplayItemsContainer(
                        index = 5,
                        isThreeColumnLayout = isThreeColumnLayout,
                        items,
                        onDeleteItemClicked,
                        onAddItemClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayItemsContainer(
    isThreeColumnLayout: Boolean,
    items: List<DisplayItem>,
    onDeleteItemClicked: (DisplayItem) -> Unit,
    onAddItemClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.take(if (isThreeColumnLayout) 2 else 3).forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FText(
                        text = stringResource(item.displayableCapability.textResource),
                        configuration = FlySightTheme.typography.sessionConfigurationText,
                        color = Color.Green
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        onDeleteItemClicked(item)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item"
                        )
                    }
                }
            }
            if (items.size < if (isThreeColumnLayout) 2 else 3) {
                Button(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = onAddItemClicked
                ) {
                    FText(
                        text = "Add item",
                        configuration = FlySightTheme.typography.plainScreenTextLarge
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
fun PreviewDisplayGridConfigurationScreen() {
    DisplayGridConfigurationScreen(
        referencePoints = emptyList(),
        form = rememberSessionProfileForm(
            initialConfiguration = SessionProfile.default().copy(
                displayGrid = DisplayGrid.ThreeOnEachSide
            )
        ),
        onDismiss = {}
    )
}
