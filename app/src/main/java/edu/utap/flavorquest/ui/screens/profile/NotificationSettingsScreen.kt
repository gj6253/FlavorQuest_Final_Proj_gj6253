package edu.utap.flavorquest.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.utap.flavorquest.ui.theme.Teal800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    var recipeReminders by remember { mutableStateOf(true) }
    var dailySuggestions by remember { mutableStateOf(false) }
    var promotionalOffers by remember { mutableStateOf(true) }
    var communityUpdates by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Notification Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Teal800,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            NotificationSwitch(
                title = "Recipe Reminders",
                description = "Get notified about your saved recipes and meal plans.",
                checked = recipeReminders,
                onCheckedChange = { recipeReminders = it }
            )

            NotificationSwitch(
                title = "Daily Suggestions",
                description = "Receive personalized meal ideas every morning.",
                checked = dailySuggestions,
                onCheckedChange = { dailySuggestions = it }
            )

            NotificationSwitch(
                title = "Promotional Offers",
                description = "Stay updated on discounts from nearby restaurants.",
                checked = promotionalOffers,
                onCheckedChange = { promotionalOffers = it }
            )

            NotificationSwitch(
                title = "Community Updates",
                description = "News about new features and flavor trends.",
                checked = communityUpdates,
                onCheckedChange = { communityUpdates = it }
            )
        }
    }
}

@Composable
private fun NotificationSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
}
