package com.example.ui.screens.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InterviewQuestion
import com.example.data.models.InterviewSession
import com.example.data.repositories.InterviewRepository
import com.example.data.services.BillingService
import com.example.data.services.GeminiService
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AiPurple
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SoftBluePrimary
import com.example.ui.theme.WarmBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterviewSetupScreen(
    repository: InterviewRepository,
    geminiService: GeminiService,
    billingService: BillingService,
    initialJobTitle: String? = null,
    onStartSession: (String) -> Unit,
    onViewReport: (String) -> Unit
) {
    val candidateProfile by repository.candidateProfile.collectAsState(initial = null)
    val pastSessions by repository.allSessions.collectAsState(initial = emptyList())
    val subscriptionState by billingService.subscriptionState.collectAsState()
    val scope = rememberCoroutineScope()

    var roleTitle by remember { mutableStateOf(initialJobTitle ?: candidateProfile?.targetRole ?: "AI/ML Engineer") }
    var selectedType by remember { mutableStateOf("Technical") }
    var selectedDifficulty by remember { mutableStateOf("Advanced") }
    var selectedDuration by remember { mutableIntStateOf(45) }
    var voiceModeEnabled by remember { mutableStateOf(true) }
    var isStarting by remember { mutableStateOf(false) }

    val interviewTypes = listOf("Technical", "Behavioral", "HR", "Mixed")
    val difficultyLevels = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    val durations = listOf(15, 30, 45)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mock Interviews",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            )
            Text(
                text = "Configure your AI mock interview loop with dynamic follow-ups",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CharcoalTextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Target Role / Job Title",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_role_textfield"),
                        placeholder = { Text("e.g., AI/ML Engineer, Senior Fullstack") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftBluePrimary,
                            unfocusedBorderColor = OutlineBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interview Type Selection
                    Text(
                        text = "Interview Type",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interviewTypes.forEach { type ->
                            SelectableChip(
                                text = type,
                                isSelected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Difficulty Level
                    Text(
                        text = "Difficulty Level",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        difficultyLevels.forEach { diff ->
                            SelectableChip(
                                text = diff,
                                isSelected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Duration
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        durations.forEach { dur ->
                            SelectableChip(
                                text = "$dur min",
                                isSelected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Mode Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarmBackground)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsVoice,
                                contentDescription = null,
                                tint = SoftBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Voice Practice Mode",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                )
                                Text(
                                    text = "Speak answers with audio interviewer",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CharcoalTextSecondary
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = voiceModeEnabled,
                            onCheckedChange = { voiceModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SoftBluePrimary
                            ),
                            modifier = Modifier.testTag("voice_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Start CTA
                    Button(
                        onClick = {
                            isStarting = true
                            scope.launch {
                                billingService.consumeMockInterview()
                                val sessionId = UUID.randomUUID().toString()
                                val session = InterviewSession(
                                    id = sessionId,
                                    jobTitle = roleTitle.ifBlank { "AI/ML Engineer" },
                                    type = selectedType,
                                    difficulty = selectedDifficulty,
                                    durationMinutes = selectedDuration,
                                    startedAt = System.currentTimeMillis()
                                )
                                repository.createSession(session)

                                // Generate questions
                                val questions = geminiService.generateInterviewQuestions(
                                    jobTitle = roleTitle,
                                    type = selectedType,
                                    difficulty = selectedDifficulty,
                                    durationMinutes = selectedDuration,
                                    candidateProfile = candidateProfile
                                )

                                val entities = questions.mapIndexed { idx, qText ->
                                    InterviewQuestion(
                                        sessionId = sessionId,
                                        questionText = qText,
                                        category = selectedType,
                                        difficulty = selectedDifficulty,
                                        orderIndex = idx
                                    )
                                }
                                repository.addQuestions(entities)
                                isStarting = false
                                onStartSession(sessionId)
                            }
                        },
                        enabled = !isStarting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_mock_interview_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                    ) {
                        if (isStarting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Preparing Interview Session...")
                        } else {
                            Text(
                                text = "Start Interview Session",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader(title = "Interview History & Sessions")
        }

        if (pastSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No past interview sessions yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalTextSecondary
                            )
                        )
                        Text(
                            text = "Configure above to launch your first AI mock session.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CharcoalTextMuted
                            )
                        )
                    }
                }
            }
        } else {
            items(pastSessions) { session ->
                val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                    .format(Date(session.startedAt))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (session.status == "COMPLETED") {
                                onViewReport(session.id)
                            } else {
                                onStartSession(session.id)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.jobTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${session.type} • ${session.difficulty} • ${session.durationMinutes}m",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CharcoalTextSecondary
                                )
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CharcoalTextMuted
                                )
                            )
                        }

                        if (session.status == "COMPLETED") {
                            ScoreBadge(score = session.overallScore)
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "In Progress",
                                    color = SoftBluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PrimaryContainer else WarmBackground)
            .border(
                1.dp,
                if (isSelected) SoftBluePrimary else OutlineBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) SoftBluePrimary else CharcoalTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
