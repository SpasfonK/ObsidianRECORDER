package com.spasfonk.obsidianrecorder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spasfonk.obsidianrecorder.ui.RecordingCategory
import com.spasfonk.obsidianrecorder.ui.RecordingItem
import com.spasfonk.obsidianrecorder.ui.theme.ElectricBlue
import com.spasfonk.obsidianrecorder.ui.theme.EmeraldAccent
import com.spasfonk.obsidianrecorder.ui.theme.ObsidianBorder
import com.spasfonk.obsidianrecorder.ui.theme.ObsidianSurface
import com.spasfonk.obsidianrecorder.ui.theme.TextPrimary
import com.spasfonk.obsidianrecorder.ui.theme.TextSecondary

@Composable
fun RecordingCard(
    item: RecordingItem,
    isPlaying: Boolean,
    onTogglePlayback: () -> Unit,
    onOpenActions: () -> Unit,
    onSetCategory: (RecordingCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (item.category != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.category.label,
                                color = EmeraldAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        EmeraldAccent.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(
                            text = item.dateLabel,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "\u00b7 ${item.sizeLabel}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = item.durationLabel,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onTogglePlayback,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isPlaying) EmeraldAccent else ElectricBlue.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lire",
                            tint = if (isPlaying) androidx.compose.ui.graphics.Color.White else ElectricBlue
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { categoryMenuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Catégories",
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Réunion") },
                                onClick = {
                                    categoryMenuExpanded = false
                                    onSetCategory(RecordingCategory.REUNION)
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Groups, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Note vocale") },
                                onClick = {
                                    categoryMenuExpanded = false
                                    onSetCategory(RecordingCategory.NOTE_VOCALE)
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Mic, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Favori") },
                                onClick = {
                                    categoryMenuExpanded = false
                                    onSetCategory(RecordingCategory.FAVORI)
                                },
                                leadingIcon = {
                                    Icon(
                                        if (item.category == RecordingCategory.FAVORI) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = null
                                    )
                                }
                            )
                            if (item.category != null) {
                                DropdownMenuItem(
                                    text = { Text("Retirer la catégorie") },
                                    onClick = {
                                        categoryMenuExpanded = false
                                        onSetCategory(null)
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onOpenActions, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Plus d'actions",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.transcriptPreview,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}