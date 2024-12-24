package fr.hozakan.flysightcompanion.composablecommons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
public fun ExpandableColumn(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    headerComposable: @Composable RowScope.(Boolean) -> Unit,
    isExpandable: Boolean = true,
    contentComposable: @Composable ColumnScope.() -> Unit
) {
    var contentExpanded by remember { mutableStateOf(expanded) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val clickableModifier = if (isExpandable) Modifier.clickable { contentExpanded = !contentExpanded } else Modifier
        Row(
            modifier = Modifier
                .requiredHeight(64.dp)
                .fillMaxWidth()
                .then(clickableModifier)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (!contentExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = stringResource(id = if (!contentExpanded) R.string.misc_expand_more else R.string.misc_reduce)
            )
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            headerComposable(contentExpanded)
        }
        if (contentExpanded) {
            Column {
                contentComposable()
            }
        }
    }
}
