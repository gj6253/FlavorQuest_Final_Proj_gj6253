package edu.utap.flavorquest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.ui.theme.Teal700

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipSelector(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onOptionToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleSelection: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option in selectedOptions
                SelectableChip(
                    text = option,
                    isSelected = isSelected,
                    onClick = { onOptionToggled(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Teal700 else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, if (isSelected) Teal700 else MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 8.dp else 0.dp,
                    bottomStart = if (index == 0) 8.dp else 0.dp,
                    topEnd = if (index == tabs.lastIndex) 8.dp else 0.dp,
                    bottomEnd = if (index == tabs.lastIndex) 8.dp else 0.dp
                ),
                color = if (isSelected) Teal700 else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, Teal700),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tab,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
