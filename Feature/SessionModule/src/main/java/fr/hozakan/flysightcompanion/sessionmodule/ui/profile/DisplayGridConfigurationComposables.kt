package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
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
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItem
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import timber.log.Timber

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
            .fillMaxSize(),
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
            DisplayGrid.TwoByTwo -> TwoByTwoLayout()
            DisplayGrid.TwoOnEachSide -> TwoOnEachSideLayout()
            DisplayGrid.ThreeOnEachSide -> ThreeOnEachSideLayout()
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
                        form.addDisplayItem(DisplayItem(
                            displayableCapability = item,
                            caseIndex = addItemClickedInfo!!.first,
                            indexInCase = addItemClickedInfo!!.second
                        ))
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
                    form.addDisplayItem(DisplayItem(
                        displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                        caseIndex = addDstToRefPoint!!.first,
                        indexInCase = addDstToRefPoint!!.second,
                        bag = DisplayItemBundle.dstToRefPt(item)
                    ))
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
            .windowInsetsPadding(WindowInsets.displayCutout)
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
            .weight(1f)
            .fillMaxHeight()
            .border(2.dp, Color.Green),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//            for (i in 0 until 3) {
//                DisplayItemsContainer(
//                    val items = displayItems.filter { it.caseIndex == i }.sortedBy { it.indexInCase }
        displayItems.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FText(
                    text = stringResource(item.displayableCapability.textResource),
                    configuration = FlySightTheme.typography.sessionPlayerText,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
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
        //                )
//            }
    }
}

@Composable
private fun InlineRightLayout() {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content with weight 6
        Box(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            FText(
                text = "Main Content Area",
                configuration = FlySightTheme.typography.plainScreenTextLarge
            )
        }

        // Right column with weight 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display items can be added here
//            DisplayItemContainer("Item 1")
//            DisplayItemContainer("Item 2")
//            DisplayItemContainer("Item 3")
        }
    }
}

@Composable
private fun TwoByTwoLayout() {
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
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(
                    text = "Top Left",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
            }

            // Top right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(
                    text = "Top Right",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
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
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(
                    text = "Bottom Left",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
            }

            // Bottom right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(
                    text = "Bottom Right",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
            }
        }
    }
}

@Composable
private fun TwoOnEachSideLayout() {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left column with weight 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "L1", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "L2", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }
        }

        // Main content with weight 5
        Box(
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
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
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two centered boxes of the same size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "R1", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "R2", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }
        }
    }
}

@Composable
private fun ThreeOnEachSideLayout() {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left column with weight 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Three centered boxes of the same size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "L1", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "L2", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "L3", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }
        }

        // Main content with weight 5
        Box(
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
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
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Three centered boxes of the same size
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "R1", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "R2", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                FText(text = "R3", configuration = FlySightTheme.typography.plainScreenTextLarge)
            }
        }
    }
}

@Composable
private fun DisplayItemsContainer(items: List<DisplayItem>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
//            .border(width = 2.dp, color = Color.Green),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 2.dp, color = Color.Black)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            items.forEach { item ->
//                FText(
//                    text = stringResource(item.displayableCapability.textResource),
//                    configuration = FlySightTheme.typography.plainScreenTextMedium
//                )
//            }
        }
    }
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
fun PreviewDisplayGridConfigurationScreen() {
    DisplayGridConfigurationScreen(
        referencePoints = emptyList(),
        form = rememberSessionProfileForm(initialConfiguration = SessionProfile.default()),
        onDismiss = {}
    )
}
