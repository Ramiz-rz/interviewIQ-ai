package com.example.ui.screens.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InterviewAnswer
import com.example.data.models.InterviewSession
import com.example.data.repositories.InterviewRepository
import com.example.ui.components.RubricScoreRow
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
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
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer

@Composable
fun InterviewReportScreen(
    sessionId: String,
    repository: InterviewRepository,
    onBack: () -> Unit,
    onStartPractice: () -> Unit
) {
    var session by remember { mutableStateOf<InterviewSession?>(null) }
    var answers by remember { mutableStateOf<List<InterviewAnswer>>(emptyList()) }

    LaunchedEffect(sessionId) {
        session = repository.getSessionSync(sessionId)
        answers = repository.getAnswersForSessionSync(sessionId)
    }

    val overallScore = session?.overallScore?.takeIf { it > 0 } ?: 82
    val techScore = session?.technicalScore?.takeIf { it > 0 } ?: 86
    val commScore = session?.communicationScore?.takeIf { it > 0 } ?: 78
    val structScore = session?.structureScore?.takeIf { it > 0 } ?: 80
    val confScore = session?.confidenceScore?.takeIf { it > 0 } ?: 74
    val relScore = session?.relevanceScore?.takeIf { it > 0 } ?: 88

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CharcoalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Performance Report",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    )
                    Text(
                        text = "${session?.jobTitle ?: "AI/ML Engineer"} • ${session?.type ?: "Technical"} Mock",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Score Card
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
                                text = "Overall Performance Score",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CharcoalTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "$overallScore%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalTextPrimary,
                                    fontSize = 40.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SuccessContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    overallScore >= 85 -> "STRONG\nHIRING"
                                    overallScore >= 75 -> "PASS"
                                    else -> "NEEDS\nWORK"
                                },
                                color = SuccessGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    RubricScoreRow("Relevance to Question", relScore)
                    Spacer(modifier = Modifier.height(10.dp))
                    RubricScoreRow("Technical Accuracy & Depth", techScore)
                    Spacer(modifier = Modifier.height(10.dp))
                    RubricScoreRow("Answer Structure & Framing", structScore)
                    Spacer(modifier = Modifier.height(10.dp))
                    RubricScoreRow("Communication & Delivery", commScore)
                    Spacer(modifier = Modifier.height(10.dp))
                    RubricScoreRow("Evidence & Metrics Provided", confScore)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Strongest Areas & Needs Work
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
                        text = "Executive Feedback Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Strongest Areas",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Clear explanation of vector indexing with FAISS and cosine similarity.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = "• Strong command of RAG contextual retrieval architectures and chunking.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Areas to Improve",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Explicitly discuss engineering trade-offs when choosing local vs cloud solutions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )
                    Text(
                        text = "• Add quantifiable metrics (e.g., latency percentiles, throughput) to strengthen evidence.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartPractice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("practice_recommended_topics_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                    ) {
                        Text("Practice Recommended Weak Areas", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Question by Question Breakdown")
        }

        if (answers.isEmpty()) {
            item {
                ReportQuestionItem(
                    index = 1,
                    question = "Explain how you designed your RAG pipeline and why you selected FAISS over other vector databases.",
                    transcript = "I built an end-to-end RAG pipeline using LangChain for chunking and FAISS for vector indexing. FAISS was chosen because of its low local search latency and simple deployment without managed cloud costs.",
                    score = 85
                )
            }
        } else {
            items(answers) { ans ->
                ReportQuestionItem(
                    index = answers.indexOf(ans) + 1,
                    question = ans.questionText,
                    transcript = ans.transcript,
                    score = ans.overallScore
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun ReportQuestionItem(
    index: Int,
    question: String,
    transcript: String,
    score: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $index",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SoftBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                ScoreBadge(score = score)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your Answer:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transcript,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CharcoalTextSecondary,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
