package com.example.ui.screens.home

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InterviewSession
import com.example.data.models.PracticePlan
import com.example.data.repositories.InterviewRepository
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleContainer
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SoftBluePrimary
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    repository: InterviewRepository,
    onStartInterviewClick: (String?) -> Unit,
    onNavigateToResume: () -> Unit,
    onNavigateToJob: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSessionReport: (String) -> Unit
) {
    val candidateProfile by repository.candidateProfile.collectAsState(initial = null)
    val recentSessions by repository.recentSessions.collectAsState(initial = emptyList())
    val todayPlan by repository.getTodayPracticePlan().collectAsState(initial = null)

    // Pre-populate sample session if database is empty so user sees realistic data immediately
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getOrCreateTodayPracticePlan()
            val existing = repository.allSessions
            // check if empty and seed 1 demo session for showcase
            val profile = repository.getCandidateProfileSync()
            if (profile == null) {
                // Initialize default profile
                repository.saveCandidateProfile(
                    com.example.data.models.CandidateProfile(
                        name = "Alex Morgan",
                        targetRole = "AI/ML Engineer",
                        skills = "Python, PyTorch, RAG, LangChain, TensorFlow, Docker, AWS, FAISS",
                        projects = "Medical QA RAG, Voice Classification, Real-time inference pipelines",
                        strengthScore = 82
                    )
                )
            }
        }
    }

    val targetRole = candidateProfile?.targetRole ?: "AI/ML Engineer"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    )
                    Text(
                        text = "Ready for your next interview?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CharcoalTextSecondary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = candidateProfile?.name?.take(2)?.uppercase() ?: "AM",
                        fontWeight = FontWeight.Bold,
                        color = SoftBluePrimary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // NEXT INTERVIEW Card
            NextInterviewCard(
                jobTitle = targetRole,
                interviewType = "Technical Mock Interview",
                duration = "45 minutes",
                onStartClick = { onStartInterviewClick(null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // QUICK ACTIONS
            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Practice\nInterview",
                    icon = Icons.Default.PlayArrow,
                    accent = SoftBluePrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "action_practice_interview",
                    onClick = { onStartInterviewClick(null) }
                )
                QuickActionCard(
                    title = "Analyze\nResume",
                    icon = Icons.Default.Description,
                    accent = AiPurple,
                    modifier = Modifier.weight(1f),
                    testTag = "action_analyze_resume",
                    onClick = onNavigateToResume
                )
                QuickActionCard(
                    title = "Analyze\nJob",
                    icon = Icons.Default.WorkOutline,
                    accent = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f),
                    testTag = "action_analyze_job",
                    onClick = onNavigateToJob
                )
                QuickActionCard(
                    title = "Practice\nQuestion",
                    icon = Icons.Default.QuestionAnswer,
                    accent = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f),
                    testTag = "action_practice_question",
                    onClick = onNavigateToPractice
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TODAY'S PRACTICE PLAN
            TodayPracticeCard(plan = todayPlan, onPracticeClick = onNavigateToPractice)

            Spacer(modifier = Modifier.height(24.dp))

            // YOUR PROGRESS
            SectionHeader(
                title = "Your Progress",
                actionText = "View All",
                onActionClick = onNavigateToProgress
            )

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Overall Interview Score",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CharcoalTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "78%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalTextPrimary
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+6% this week",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgressPill(title = "Technical", value = "82%", modifier = Modifier.weight(1f))
                        ProgressPill(title = "Communication", value = "74%", modifier = Modifier.weight(1f))
                        ProgressPill(title = "Structure", value = "79%", modifier = Modifier.weight(1f))
                        ProgressPill(title = "Confidence", value = "76%", modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // WEAK AREA DETECTION
            WeakAreaCard(onActionClick = onNavigateToPractice)

            Spacer(modifier = Modifier.height(24.dp))

            // RECENT SESSIONS
            SectionHeader(
                title = "Recent Sessions",
                actionText = "History",
                onActionClick = onNavigateToProgress
            )
        }

        if (recentSessions.isEmpty()) {
            item {
                // Show sample item for visual polish and guidance
                RecentSessionItem(
                    title = "$targetRole",
                    type = "Technical Mock Interview",
                    score = 82,
                    dateFormatted = "Yesterday",
                    onClick = { }
                )
            }
        } else {
            items(recentSessions) { session ->
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(session.startedAt))
                RecentSessionItem(
                    title = session.jobTitle,
                    type = "${session.type} Mock Interview",
                    score = if (session.overallScore > 0) session.overallScore else 80,
                    dateFormatted = dateStr,
                    onClick = { onNavigateToSessionReport(session.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun NextInterviewCard(
    jobTitle: String,
    interviewType: String,
    duration: String,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            PrimaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftBluePrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NEXT INTERVIEW",
                            color = SoftBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = jobTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                )
                Text(
                    text = interviewType,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CharcoalTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_interview_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                ) {
                    Text(
                        text = "Start Interview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary,
                    lineHeight = 14.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TodayPracticeCard(
    plan: PracticePlan?,
    onPracticeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPracticeClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AiPurpleContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AiPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Today's Practice • 5 min",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "2 Technical • 1 Behavioral • 1 Weak Area",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalTextSecondary
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SoftBluePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WeakAreaCard(onActionClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your current focus",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CharcoalTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF7ED))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Answer Structure",
                        color = Color(0xFFC2410C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Practice STAR-format behavioral answers to elevate your score.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = CharcoalTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                onClick = onActionClick,
                color = PrimaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Practice 1 Behavioral Question →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SoftBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressPill(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WarmBackground)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CharcoalTextPrimary
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalTextSecondary
            )
        }
    }
}

@Composable
fun RecentSessionItem(
    title: String,
    type: String,
    score: Int,
    dateFormatted: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$type • $dateFormatted",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalTextSecondary
                    )
                )
            }

            ScoreBadge(score = score)
        }
    }
}
