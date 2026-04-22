package edu.utap.flavorquest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.ui.theme.Teal700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceSelector(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Order by Price:"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (level in 1..4) {
                val priceText = "$".repeat(level)
                val isSelected = level <= selectedLevel

                Surface(
                    onClick = { onLevelSelected(level) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Teal700 else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, if (isSelected) Teal700 else MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
