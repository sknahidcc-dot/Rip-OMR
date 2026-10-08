package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DeveloperInfoDialog
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val sensitivity by viewModel.sensitivity.collectAsStateWithLifecycle()
    val useBnDigits by viewModel.useBengaliDigits.collectAsStateWithLifecycle()
    val allowNegativeTotal by viewModel.allowNegativeTotal.collectAsStateWithLifecycle()

    var showDevDialog by remember { mutableStateOf(false) }

    if (showDevDialog) {
        DeveloperInfoDialog(onDismiss = { showDevDialog = false })
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AmoledSurface)
                        .border(1.dp, AmoledCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "সেটিংস",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "স্ক্যান সংবেদনশীলতা ও পছন্দসমূহ",
                        color = CyanAccent,
                        fontSize = 11.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Scanner Sensitivity
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Sensitivity",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "বৃত্ত ভরাট সংবেদনশীলতা (Sensitivity)",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = String.format(Locale.US, "%.2f", sensitivity),
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "কম মান = কেবল অত্যন্ত গাঢ় ভরাট গ্রহণ করবে। বেশি মান = হালকা বা পেন্সিল ভরাটও শনাক্ত করবে। (ডিফল্ট: ০.৬৮)",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Slider(
                                value = sensitivity,
                                onValueChange = { viewModel.sensitivity.value = it },
                                valueRange = 0.50f..0.85f,
                                steps = 34,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAccent,
                                    activeTrackColor = CyanAccent,
                                    inactiveTrackColor = AmoledCardBorder
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "ডিফল্টে রিসেট করুন",
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { viewModel.sensitivity.value = 0.68f }
                                )
                            }
                        }
                    }
                }

                // Section: App Preferences
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "সাধারণ পছন্দসমূহ",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            // Toggle: Bengali Digits
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "বাংলা সংখ্যা ব্যবহার করুন",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "নম্বর ও রোল বাংলা অঙ্কে (০-৯) প্রদর্শন করবে",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = useBnDigits,
                                    onCheckedChange = { viewModel.useBengaliDigits.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmoledBlack,
                                        checkedTrackColor = CyanAccent
                                    )
                                )
                            }

                            // Toggle: Allow Negative Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ঋণাত্মক মোট স্কোর অনুমোদন",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "বন্ধ থাকলে শূন্যের (০) নিচে নম্বর নামবে না",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = allowNegativeTotal,
                                    onCheckedChange = { viewModel.allowNegativeTotal.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmoledBlack,
                                        checkedTrackColor = CyanAccent
                                    )
                                )
                            }
                        }
                    }
                }

                // Section: Utilities & About
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column {
                            SettingsNavRow(
                                icon = Icons.Default.Description,
                                title = "অফিশিয়াল ওএমআর শিট দেখুন",
                                subtitle = "A4 সাইজ প্রিন্ট ও লেআউট ফরম্যাট",
                                onClick = { onNavigate(Screen.PrintableSheet) }
                            )

                            SettingsNavRow(
                                icon = Icons.Default.Info,
                                title = "ডেভেলপার তথ্য",
                                subtitle = "Nahid Hasan • University of Barishal",
                                onClick = { showDevDialog = true }
                            )
                        }
                    }
                }

                // App Info & Version
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RIP OMR v1.0.0 (AMOLED Edition)",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "১০০% অফলাইন • ক্যামেরা ও গ্যালারি সমর্থিত",
                            color = TextTertiary.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = TextTertiary, fontSize = 11.sp)
        }
        Text(text = "➔", color = TextTertiary, fontSize = 12.sp)
    }
}
