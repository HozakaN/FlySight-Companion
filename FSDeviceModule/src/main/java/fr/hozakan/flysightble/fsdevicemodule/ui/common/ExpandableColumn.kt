package fr.hozakan.flysightble.fsdevicemodule.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import fr.hozakan.flysightble.fsdevicemodule.R

@Composable
public fun ExpandableColumn(
    headerComposable: @Composable (Boolean) -> Unit,
    contentComposable: @Composable () -> Unit
) {
    var contentExpanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .requiredHeight(64.dp)
                .fillMaxWidth()
                .clickable { contentExpanded = !contentExpanded }
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
            contentComposable()
        }
    }
}
