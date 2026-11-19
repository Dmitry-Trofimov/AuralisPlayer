package com.daimonxx.auralis.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.daimonxx.auralis.playback.AuralisEqualizer
import com.daimonxx.auralis.playback.EqualizerPreset
import com.daimonxx.auralis.ui.components.GlassSurface
import com.daimonxx.auralis.ui.theme.AuralisColors
import kotlin.math.roundToInt

@Composable
fun EqualizerPanel(
    enabled: Boolean,
    selectedPresetId: String,
    gainsDb: List<Float>,
    onEnabledChange: (Boolean) -> Unit,
    onPresetSelected: (EqualizerPreset) -> Unit,
    onGainChanged: (Int, Float) -> Unit,
    onReset: () -> Unit,
    hasCurrentTrack: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassSurface(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(43.dp).clip(RoundedCornerShape(15.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFB68AFF), Color(0xFF67458E)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = Color.White)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("AURALIS EQ", color = AuralisColors.Text, fontSize = 10.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.Bold)
                        Text("10-band studio equalizer", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                    Switch(checked = enabled, onCheckedChange = onEnabledChange)
                }
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (enabled) "Эквалайзер включён" else "Эквалайзер выключен", color = if (enabled) AuralisColors.PurpleBright else AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("±12 dB", color = AuralisColors.Muted, fontSize = 11.sp)
                }
            }
        }

        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("ПРЕДУСТАНОВКИ", color = AuralisColors.Text, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("СБРОС", color = AuralisColors.PurpleBright, fontSize = 10.sp, letterSpacing = 1.3.sp, modifier = Modifier.clickable(onClick = onReset))
                Icon(Icons.Rounded.Refresh, contentDescription = "Сбросить эквалайзер", tint = AuralisColors.PurpleBright, modifier = Modifier.padding(start = 3.dp).size(14.dp).clickable(onClick = onReset))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuralisEqualizer.presets.forEach { preset ->
                    PresetChip(
                        title = preset.title,
                        selected = selectedPresetId == preset.id,
                        onClick = { onPresetSelected(preset) }
                    )
                }
            }
        }

        GlassSurface(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 17.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Частоты", color = AuralisColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("РУЧНАЯ НАСТРОЙКА", color = AuralisColors.Muted, fontSize = 8.sp, letterSpacing = 1.1.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuralisEqualizer.frequenciesHz.indices.forEach { index ->
                        val value = gainsDb.getOrElse(index) { 0f }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${if (value > 0f) "+" else ""}${value.roundToInt()}",
                                color = if (enabled) AuralisColors.PurpleBright else AuralisColors.Muted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            VerticalEqSlider(
                                value = value,
                                enabled = enabled,
                                onValueChange = { onGainChanged(index, it) },
                                modifier = Modifier.padding(top = 7.dp, bottom = 7.dp).width(25.dp).height(135.dp)
                            )
                            Text(
                                text = AuralisEqualizer.frequencyLabels[index],
                                color = AuralisColors.Muted,
                                fontSize = 8.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 13.dp, start = 3.dp, end = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("−12 dB", color = AuralisColors.Muted, fontSize = 9.sp)
                    Text("0 dB", color = AuralisColors.Muted, fontSize = 9.sp)
                    Text("+12 dB", color = AuralisColors.Muted, fontSize = 9.sp)
                }
            }
        }

        GlassSurface(modifier = Modifier.fillMaxWidth(), cornerRadius = 21.dp) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Headphones, contentDescription = null, tint = AuralisColors.PurpleBright, modifier = Modifier.size(23.dp))
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Обработка звука устройства", color = AuralisColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            !hasCurrentTrack -> "Профиль сохранён и применится при запуске музыки."
                            else -> "Реальные фильтры задаёт аудиодрайвер; на части устройств и эмуляторов системный EQ может быть недоступен."
                        },
                        color = AuralisColors.Muted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color(0x665E467F) else Color(0x20FFFFFF))
            .border(1.dp, if (selected) Color(0x99D3BFFF) else Color.White.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (selected) Color.White else AuralisColors.Muted, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun VerticalEqSlider(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (enabled) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.10f)
    val activeColor = if (enabled) AuralisColors.PurpleBright else AuralisColors.Muted.copy(alpha = 0.45f)
    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset ->
                        val top = 8.dp.toPx()
                        val usable = (size.height - 16.dp.toPx()).coerceAtLeast(1f)
                        val normalized = (1f - ((offset.y - top) / usable)).coerceIn(0f, 1f)
                        onValueChange((-12f + normalized * 24f).coerceIn(-12f, 12f))
                    }
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val top = 8.dp.toPx()
                            val usable = (size.height - 16.dp.toPx()).coerceAtLeast(1f)
                            val normalized = (1f - ((offset.y - top) / usable)).coerceIn(0f, 1f)
                            onValueChange((-12f + normalized * 24f).coerceIn(-12f, 12f))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val top = 8.dp.toPx()
                            val usable = (size.height - 16.dp.toPx()).coerceAtLeast(1f)
                            val normalized = (1f - ((change.position.y - top) / usable)).coerceIn(0f, 1f)
                            onValueChange((-12f + normalized * 24f).coerceIn(-12f, 12f))
                        }
                    )
                }
            }
    ) {
        Canvas(Modifier.fillMaxWidth().height(135.dp)) {
            val centerX = size.width / 2f
            val top = 8.dp.toPx()
            val bottom = size.height - 8.dp.toPx()
            val zeroY = top + (bottom - top) / 2f
            val thumbY = bottom - ((value + 12f) / 24f) * (bottom - top)
            drawLine(trackColor, Offset(centerX, top), Offset(centerX, bottom), 4.dp.toPx(), cap = StrokeCap.Round)
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0xFF8E69C9), activeColor, Color(0xFFB5A0FF))),
                start = Offset(centerX, zeroY),
                end = Offset(centerX, thumbY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(Color.White.copy(alpha = 0.35f), Offset(centerX - 6.dp.toPx(), zeroY), Offset(centerX + 6.dp.toPx(), zeroY), 1.dp.toPx())
            drawCircle(Color.White.copy(alpha = if (enabled) 0.95f else 0.35f), radius = 7.dp.toPx(), center = Offset(centerX, thumbY))
            drawCircle(activeColor.copy(alpha = 0.70f), radius = 10.dp.toPx(), center = Offset(centerX, thumbY), style = Stroke(width = 1.2.dp.toPx()))
        }
    }
}
