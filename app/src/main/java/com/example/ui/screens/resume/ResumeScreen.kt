package com.example.ui.screens.resume

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.models.CandidateProfile
import com.example.data.models.ResumeStrengthAnalysis
import com.example.data.repositories.InterviewRepository
import com.example.data.services.GeminiService
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeScreen(
    repository: InterviewRepository,
    geminiService: GeminiService,
    onBack: () -> Unit
) {
    val existingProfile by repository.candidateProfile.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var resumeInputText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var strengthAnalysis by remember { mutableStateOf<ResumeStrengthAnalysis?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Editable fields
    var name by remember { mutableStateOf("Alex Morgan") }
    var targetRole by remember { mutableStateOf("AI/ML Engineer") }
    var experienceLevel by remember { mutableStateOf("Senior") }
    var summary by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }
    var achievements by remember { mutableStateOf("") }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    // Populate from existing
    LaunchedEffect(existingProfile) {
        existingProfile?.let { p ->
            name = p.name
            targetRole = p.targetRole
            experienceLevel = p.experienceLevel
            summary = p.summary
            skills = p.skills
            experience = p.experience
            education = p.education
            projects = p.projects
            achievements = p.achievements
            if (p.strengthScore > 0 && strengthAnalysis == null) {
                strengthAnalysis = ResumeStrengthAnalysis(
                    score = p.strengthScore,
                    keywords = p.skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                )
            }
        }
    }

    val sampleResumeText = """
Alex Morgan
San Francisco, CA | alex.morgan@email.com | github.com/alexmorgan

PROFESSIONAL SUMMARY
Senior Machine Learning & Software Engineer with 5+ years of production experience architecting RAG pipelines, dense vector search, and LLM inference microservices.

CORE SKILLS
Languages & Frameworks: Python, PyTorch, LangChain, TensorFlow, Flutter, FastAPI, SQL
AI/ML: RAG, Vector Search, FAISS, Embeddings, Cross-Encoder Reranking, Fine-Tuning
Infrastructure: Docker, AWS (SageMaker, S3, ECS), Kubernetes, CI/CD, Linux

PROFESSIONAL EXPERIENCE
Senior Machine Learning Engineer | NeuralScale AI (2022 - Present)
- Designed and deployed an end-to-end Medical QA RAG retrieval pipeline using FAISS and LangChain, serving 50,000+ daily queries with sub-350ms p99 latency.
- Reduced retrieval hallucination rate by 34% by introducing semantic chunking and cross-encoder reranking.
- Containerized models with Docker and deployed scalable inference on AWS ECS clusters.

Machine Learning Engineer | Apex Data Labs (2020 - 2022)
- Built computer vision & speech classification models for acoustic health diagnostics.
- Streamlined model training pipelines with PyTorch, cutting experiment turnaround time by 40%.

EDUCATION
B.S. in Computer Science | University of California, Berkeley (2016 - 2020)

PROJECTS
- Medical QA RAG: Context-aware biomedical question answering over 50,000 PubMed articles.
- TherapyLink: AI-assisted clinical note summarization platform.
- Parkinson's Voice Detection: Audio spectrogram classification model with 92% sensitivity.
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
                        text = "Resume Analyzer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                    )
                    Text(
                        text = "Extract structured candidate profile & AI strength audit",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Disclaimer notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryContainer.copy(alpha = 0.6f))
                    .border(1.dp, SoftBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SoftBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI-generated assessment for interview coaching. Does not guarantee hiring outcomes. All extracted profile fields remain fully editable.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resume Input Card
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
                            text = "Paste Resume Content",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        )
                        OutlinedButton(
                            onClick = {
                                resumeInputText = sampleResumeText
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("load_sample_resume_button")
                        ) {
                            Text("Load Sample (Alex)", fontSize = 11.sp, color = SoftBluePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = resumeInputText,
                        onValueChange = { resumeInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("resume_input_textfield"),
                        placeholder = {
                            Text(
                                "Paste text from PDF, DOCX, or LinkedIn resume...",
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
                            val textToAnalyze = resumeInputText.ifBlank { sampleResumeText }
                            isAnalyzing = true
                            statusNotice = null
                            scope.launch {
                                val result = geminiService.analyzeResume(textToAnalyze)
                                val extracted = result.first
                                val analysis = result.second

                                name = extracted.name
                                targetRole = extracted.targetRole
                                experienceLevel = extracted.experienceLevel
                                summary = extracted.summary
                                skills = extracted.skills
                                experience = extracted.experience
                                education = extracted.education
                                projects = extracted.projects
                                achievements = extracted.achievements
                                strengthAnalysis = analysis

                                repository.saveCandidateProfile(
                                    extracted.copy(strengthScore = analysis.score)
                                )
                                isAnalyzing = false
                                statusNotice = "Resume successfully analyzed and profile updated!"
                            }
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("analyze_resume_button"),
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
                            Text("Analyzing with Gemini...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analyze Resume",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (statusNotice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusNotice!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resume Strength Audit Card
            strengthAnalysis?.let { analysis ->
                SectionHeader(title = "Resume Strength Assessment")

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
                                    text = "Resume Strength",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                )
                                Text(
                                    text = "Based on ATS clarity & technical depth",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CharcoalTextSecondary
                                    )
                                )
                            }
                            ScoreBadge(score = analysis.score, suffix = "/100")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AuditItem("Skills Clarity", analysis.skillsClarity)
                        Spacer(modifier = Modifier.height(8.dp))
                        AuditItem("Experience Quality", analysis.experienceQuality)
                        Spacer(modifier = Modifier.height(8.dp))
                        AuditItem("Project Descriptions", analysis.projectDescriptions)
                        Spacer(modifier = Modifier.height(8.dp))
                        AuditItem("Quantifiable Achievements", analysis.quantifiableAchievements)
                        Spacer(modifier = Modifier.height(8.dp))
                        AuditItem("Technical Depth", analysis.technicalDepth)
                        Spacer(modifier = Modifier.height(8.dp))
                        AuditItem("Formatting & Structure", analysis.formattingConcerns)

                        if (analysis.keywords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Detected Keywords",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                analysis.keywords.forEach { kw ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = kw,
                                            fontSize = 11.sp,
                                            color = SoftBluePrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        if (analysis.missingInformation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Missing / Recommended Additions",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            analysis.missingInformation.forEach { missing ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(WarningAmber)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = missing,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CharcoalTextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Structured Candidate Profile (Always editable!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Structured Candidate Profile",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                )
                TextButton(
                    onClick = {
                        if (isEditingProfile) {
                            // Save
                            scope.launch {
                                repository.saveCandidateProfile(
                                    CandidateProfile(
                                        id = existingProfile?.id ?: java.util.UUID.randomUUID().toString(),
                                        name = name,
                                        targetRole = targetRole,
                                        experienceLevel = experienceLevel,
                                        summary = summary,
                                        skills = skills,
                                        experience = experience,
                                        education = education,
                                        projects = projects,
                                        achievements = achievements,
                                        strengthScore = strengthAnalysis?.score ?: 82
                                    )
                                )
                                statusNotice = "Profile saved successfully!"
                            }
                        }
                        isEditingProfile = !isEditingProfile
                    }
                ) {
                    Icon(
                        imageVector = if (isEditingProfile) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SoftBluePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditingProfile) "Save Profile" else "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        color = SoftBluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    if (isEditingProfile) {
                        ProfileEditField("Full Name", name) { name = it }
                        ProfileEditField("Target Role", targetRole) { targetRole = it }
                        ProfileEditField("Experience Level", experienceLevel) { experienceLevel = it }
                        ProfileEditField("Summary", summary, singleLine = false) { summary = it }
                        ProfileEditField("Skills (comma-separated)", skills, singleLine = false) { skills = it }
                        ProfileEditField("Experience", experience, singleLine = false) { experience = it }
                        ProfileEditField("Projects", projects, singleLine = false) { projects = it }
                        ProfileEditField("Education", education) { education = it }
                    } else {
                        ProfileReadOnlyItem("Name", name)
                        ProfileReadOnlyItem("Target Role", targetRole)
                        ProfileReadOnlyItem("Experience Level", experienceLevel)
                        ProfileReadOnlyItem("Summary", summary.ifBlank { "Senior AI/ML Engineer with deep RAG and vector retrieval expertise." })
                        ProfileReadOnlyItem("Skills", skills.ifBlank { "Python, PyTorch, RAG, LangChain, TensorFlow, Docker, AWS, FAISS" })
                        ProfileReadOnlyItem("Projects", projects.ifBlank { "Medical QA RAG, Voice Classification platform" })
                        ProfileReadOnlyItem("Education", education.ifBlank { "B.S. in Computer Science" })
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun AuditItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = CharcoalTextSecondary,
                lineHeight = 16.sp
            )
        )
    }
}

@Composable
fun ProfileReadOnlyItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalTextMuted,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CharcoalTextPrimary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CharcoalTextSecondary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SoftBluePrimary,
                unfocusedBorderColor = OutlineBorder
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
