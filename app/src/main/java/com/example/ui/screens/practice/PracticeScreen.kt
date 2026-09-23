package com.example.ui.screens.practice

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AnswerFeedback
import com.example.data.models.PracticeQuestion
import com.example.data.repositories.InterviewRepository
import com.example.data.services.GeminiService
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StarFeedbackCard
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleContainer
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SoftBluePrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmBackground
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    repository: InterviewRepository,
    geminiService: GeminiService,
    onNavigateToCareerCoach: () -> Unit
) {
    val candidateProfile by repository.candidateProfile.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("All") }
    var activePracticeQuestion by remember { mutableStateOf<PracticeQuestion?>(null) }
    var practiceAnswerText by remember { mutableStateOf("") }
    var isEvaluating by remember { mutableStateOf(false) }
    var practiceFeedback by remember { mutableStateOf<AnswerFeedback?>(null) }

    val categories = listOf("All", "ML / AI", "System Design", "Behavioral", "Python", "Cloud & Infra", "HR")

    val questionBank = remember {
        listOf(
            PracticeQuestion(
                question = "How do you evaluate context precision and recall in RAG systems?",
                category = "ML / AI",
                difficulty = "Advanced",
                sampleAnswerHint = "Discuss synthetic test sets, Ragas or TruLens metrics, embedding distance thresholds, and ground-truth verification."
            ),
            PracticeQuestion(
                question = "Tell me about a time you had to handle an unexpected production incident with high user impact.",
                category = "Behavioral",
                difficulty = "Medium",
                sampleAnswerHint = "Use STAR: Situation, mitigation steps taken immediately, root cause analysis, and preventive measures implemented."
            ),
            PracticeQuestion(
                question = "How would you design a rate limiter for an inference API serving multiple tier tenants?",
                category = "System Design",
                difficulty = "Advanced",
                sampleAnswerHint = "Compare token bucket vs sliding window log in Redis, distributed locking, and graceful degradation."
            ),
            PracticeQuestion(
                question = "Explain the difference between dense and sparse embeddings in hybrid search.",
                category = "ML / AI",
                difficulty = "Advanced",
                sampleAnswerHint = "Contrast semantic cosine similarity (dense vectors like OpenAI text-embedding-3) with exact keyword BM25/Splade (sparse vectors), and reciprocal rank fusion (RRF)."
            ),
            PracticeQuestion(
                question = "Describe how the Python Global Interpreter Lock (GIL) impacts multi-threaded CPU-bound vs IO-bound tasks.",
                category = "Python",
                difficulty = "Intermediate",
                sampleAnswerHint = "Explain bytecode serialization, thread switching, why multiprocessing or asyncio is preferred for concurrency."
            ),
            PracticeQuestion(
                question = "How do you optimize Docker image layers for high-throughput containerized PyTorch deployments?",
                category = "Cloud & Infra",
                difficulty = "Advanced",
                sampleAnswerHint = "Multi-stage builds, cache mounting, minimizing CUDA driver overhead, and using slim base images."
            ),
            PracticeQuestion(
                question = "Why are you looking to leave your current role, and what attracts you to our technical vision?",
                category = "HR",
                difficulty = "Beginner",
                sampleAnswerHint = "Focus on positive career growth, desire for higher-scale AI engineering, and alignment with company goals."
            )
        )
    }

    val filteredQuestions = if (selectedCategory == "All") {
        questionBank
    } else {
        questionBank.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .padding(horizontal = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Practice & Drills",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            )
            Text(
                text = "Bite-sized 2-minute drills with instant AI rubric feedback",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CharcoalTextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2-MINUTE QUICK PRACTICE CARDS
            SectionHeader(title = "2-Minute Quick Practice")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickPracticeModeCard(
                    title = "Technical",
                    subtitle = "ML & Algorithms",
                    color = SoftBluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activePracticeQuestion = questionBank.first { it.category == "ML / AI" }
                        practiceFeedback = null
                        practiceAnswerText = ""
                    }
                )
                QuickPracticeModeCard(
                    title = "Behavioral",
                    subtitle = "STAR framework",
                    color = AiPurple,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activePracticeQuestion = questionBank.first { it.category == "Behavioral" }
                        practiceFeedback = null
                        practiceAnswerText = ""
                    }
                )
                QuickPracticeModeCard(
                    title = "System Design",
                    subtitle = "Scalability & Tradeoffs",
                    color = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activePracticeQuestion = questionBank.first { it.category == "System Design" }
                        practiceFeedback = null
                        practiceAnswerText = ""
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Career Coach Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToCareerCoach),
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AiPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = AiPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Career Coach",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        )
                        Text(
                            text = "Ask questions on salary, project framing, or gaps",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Active Interactive Practice Drill
            activePracticeQuestion?.let { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, SoftBluePrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE DRILL • ${q.category}",
                                    color = SoftBluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    activePracticeQuestion = null
                                    practiceFeedback = null
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Hint
                        Text(
                            text = "Coach Hint: ${q.sampleAnswerHint}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CharcoalTextSecondary,
                                lineHeight = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = practiceAnswerText,
                            onValueChange = { practiceAnswerText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("practice_answer_input"),
                            placeholder = { Text("Write your practice answer here...", color = CharcoalTextMuted, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftBluePrimary,
                                unfocusedBorderColor = OutlineBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (practiceAnswerText.isNotBlank()) {
                                    isEvaluating = true
                                    scope.launch {
                                        val fb = geminiService.evaluateAnswer(
                                            question = q.question,
                                            answer = practiceAnswerText,
                                            category = q.category,
                                            candidateProfile = candidateProfile
                                        )
                                        practiceFeedback = fb
                                        isEvaluating = false
                                    }
                                }
                            },
                            enabled = practiceAnswerText.isNotBlank() && !isEvaluating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_practice_drill_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                        ) {
                            if (isEvaluating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Evaluating...")
                            } else {
                                Text("Get AI Answer Evaluation", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Display feedback if evaluated
                        practiceFeedback?.let { fb ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Evaluation Score", fontWeight = FontWeight.Bold, color = CharcoalTextPrimary)
                                ScoreBadge(score = fb.overallScore, suffix = "/100")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Structure Advice: ${fb.betterStructureAdvice}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CharcoalTextSecondary)
                            )

                            if (fb.starFeedback != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                StarFeedbackCard(star = fb.starFeedback)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Question Library & Filters
            SectionHeader(title = "Question Library (${filteredQuestions.size})")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) PrimaryContainer else Color.White)
                            .border(1.dp, if (isSel) SoftBluePrimary else OutlineBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) SoftBluePrimary else CharcoalTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(filteredQuestions) { q ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activePracticeQuestion = q
                        practiceFeedback = null
                        practiceAnswerText = ""
                    },
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarmBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = q.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftBluePrimary
                            )
                        }

                        Text(
                            text = q.difficulty,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Practice Now →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SoftBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun QuickPracticeModeCard(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CharcoalTextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}
