package com.example.ui.screens.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.JobAnalysisResult
import com.example.data.models.JobProfile
import com.example.data.models.SkillMatchItem
import com.example.data.repositories.InterviewRepository
import com.example.data.services.GeminiService
import com.example.ui.components.ScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleContainer
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SoftBluePrimary
import com.example.ui.theme.SuccessContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmBackground
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobScreen(
    repository: InterviewRepository,
    geminiService: GeminiService,
    onBack: () -> Unit,
    onStartInterviewForJob: (String) -> Unit
) {
    val candidateProfile by repository.candidateProfile.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var jobInputText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<JobAnalysisResult?>(null) }
    var savedJobId by remember { mutableStateOf<String?>(null) }

    val sampleJobText = """
Senior AI/ML Engineer (Generative AI & RAG)
Company: NeuralPath Technologies
Location: San Francisco, CA (Hybrid)

About the Role:
We are seeking a Senior AI/ML Engineer to lead the architecture of enterprise-scale RAG retrieval systems and LLM orchestration. You will design low-latency vector indexes, optimize context retrieval precision, and deploy distributed machine learning workloads.

Requirements:
- 4+ years building production Machine Learning & Deep Learning systems.
- Strong proficiency in Python, PyTorch, LangChain, and dense vector databases (FAISS, Milvus, or Qdrant).
- Deep experience designing Retrieval-Augmented Generation (RAG) pipelines with semantic chunking and reranking.
- Hands-on experience with containerization (Docker) and AWS cloud infrastructure (SageMaker, ECS, S3).
- Experience with Kubernetes cluster deployment and high-throughput inference serving is preferred.
- Strong understanding of trade-offs between dense vs sparse embeddings and latency vs accuracy.
    """.trimIndent()

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
                        text = "Job Description Analyzer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    )
                    Text(
                        text = "Extract requirements & compute candidate skill gap",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Job Input Card
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paste Job Posting",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        )
                        OutlinedButton(
                            onClick = { jobInputText = sampleJobText },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("load_sample_job_button")
                        ) {
                            Text("Sample AI/ML Posting", fontSize = 11.sp, color = SoftBluePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = jobInputText,
                        onValueChange = { jobInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("job_input_textfield"),
                        placeholder = {
                            Text(
                                "Paste job responsibilities, skills, and qualifications...",
                                color = CharcoalTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftBluePrimary,
                            unfocusedBorderColor = OutlineBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val textToAnalyze = jobInputText.ifBlank { sampleJobText }
                            isAnalyzing = true
                            scope.launch {
                                val result = geminiService.analyzeJobDescription(textToAnalyze, candidateProfile)
                                analysisResult = result

                                val jobProfile = JobProfile(
                                    title = result.title,
                                    company = result.company,
                                    seniority = result.seniority,
                                    description = textToAnalyze,
                                    requiredSkills = result.requiredSkills.joinToString(", "),
                                    preferredSkills = result.preferredSkills.joinToString(", "),
                                    responsibilities = result.responsibilities,
                                    domain = result.domain,
                                    interviewTopics = result.interviewTopics.joinToString(", "),
                                    matchScore = result.matchPercentage
                                )
                                repository.saveJob(jobProfile)
                                savedJobId = jobProfile.id
                                isAnalyzing = false
                            }
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("analyze_job_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Skills with Gemini...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze & Match Skills", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            analysisResult?.let { result ->
                // SKILL GAP ANALYSIS CARD
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
                                    text = result.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                )
                                Text(
                                    text = "${result.company} • ${result.seniority}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CharcoalTextSecondary
                                    )
                                )
                            }
                            ScoreBadge(score = result.matchPercentage, suffix = "% Match")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Match Advice
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryContainer.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = result.summaryAdvice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CharcoalTextPrimary,
                                    lineHeight = 18.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Strong Matches
                        SkillSectionHeader(
                            title = "Strong Matches (${result.strongMatches.size})",
                            color = SuccessGreen,
                            badgeColor = SuccessContainer
                        )
                        result.strongMatches.forEach { match ->
                            SkillGapRow(item = match, dotColor = SuccessGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Skills to Strengthen
                        SkillSectionHeader(
                            title = "Skills to Strengthen (${result.skillsToStrengthen.size})",
                            color = WarningAmber,
                            badgeColor = WarningContainer
                        )
                        result.skillsToStrengthen.forEach { match ->
                            SkillGapRow(item = match, dotColor = WarningAmber)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Missing / Low Evidence
                        SkillSectionHeader(
                            title = "Missing / Low Evidence (${result.missingOrLowEvidence.size})",
                            color = ErrorRed,
                            badgeColor = ErrorContainer
                        )
                        result.missingOrLowEvidence.forEach { match ->
                            SkillGapRow(item = match, dotColor = ErrorRed)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interview topics
                        if (result.interviewTopics.isNotEmpty()) {
                            Text(
                                text = "Expected Interview Topics",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.interviewTopics.forEach { topic ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AiPurpleContainer)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = topic,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AiPurple
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // CTA Start tailored interview
                        Button(
                            onClick = {
                                onStartInterviewForJob(result.title)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_tailored_interview_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                        ) {
                            Text(
                                text = "Generate Tailored Mock Interview",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun SkillSectionHeader(title: String, color: Color, badgeColor: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun SkillGapRow(item: SkillMatchItem, dotColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.skill,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
            )
        }
        Text(
            text = item.reason,
            style = MaterialTheme.typography.bodySmall.copy(
                color = CharcoalTextSecondary,
                lineHeight = 16.sp
            ),
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
    }
}
