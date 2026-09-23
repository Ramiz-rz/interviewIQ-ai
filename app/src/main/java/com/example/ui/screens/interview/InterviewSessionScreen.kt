package com.example.ui.screens.interview

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.AnswerFeedback
import com.example.data.models.InterviewAnswer
import com.example.data.models.InterviewQuestion
import com.example.data.models.InterviewSession
import com.example.data.repositories.InterviewRepository
import com.example.data.services.GeminiService
import com.example.data.services.SpeechService
import com.example.ui.components.AiInterviewerAvatar
import com.example.ui.components.AnimatedWaveform
import com.example.ui.components.ScoreBadge
import com.example.ui.components.StarFeedbackCard
import com.example.ui.theme.AiPurple
import com.example.ui.theme.AiPurpleContainer
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
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

enum class InterviewStep {
    QUESTION_ACTIVE,
    ANSWERING,
    EVALUATING,
    EVALUATION_RESULT,
    FOLLOW_UP_ACTIVE
}

@Composable
fun InterviewSessionScreen(
    sessionId: String,
    repository: InterviewRepository,
    geminiService: GeminiService,
    speechService: SpeechService,
    onBack: () -> Unit,
    onFinishInterview: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf<InterviewSession?>(null) }
    var questions by remember { mutableStateOf<List<InterviewQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentStep by remember { mutableStateOf(InterviewStep.QUESTION_ACTIVE) }

    var isVoiceMode by remember { mutableStateOf(true) }
    var answerText by remember { mutableStateOf("") }
    var isEditingTranscript by remember { mutableStateOf(false) }
    var currentFeedback by remember { mutableStateOf<AnswerFeedback?>(null) }
    var currentFollowUpQuestion by remember { mutableStateOf<String?>(null) }
    var isHandlingFollowUp by remember { mutableStateOf(false) }
    val evaluatedAnswers = remember { mutableListOf<Pair<String, AnswerFeedback>>() }

    val isListening by speechService.isListening.collectAsState()
    val soundLevel by speechService.soundLevel.collectAsState()
    val speechError by speechService.errorMessage.collectAsState()

    // Permission launcher for audio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechService.startListening { recognized ->
                answerText = recognized
            }
        }
    }

    // Load session & questions
    LaunchedEffect(sessionId) {
        scope.launch {
            val s = repository.getSessionSync(sessionId)
            session = s
            val qList = repository.getQuestionsForSessionSync(sessionId)
            if (qList.isNotEmpty()) {
                questions = qList
                speechService.speak(qList[0].questionText)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechService.stopListening()
            speechService.stopSpeaking()
        }
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CharcoalTextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = session?.jobTitle ?: "Mock Interview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                )
                Text(
                    text = if (questions.isNotEmpty()) {
                        "Question ${currentQuestionIndex + 1} of ${questions.size}"
                    } else "Preparing questions...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalTextSecondary
                    )
                )
            }

            // Mode toggle (Voice vs Text)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryContainer)
                    .clickable { isVoiceMode = !isVoiceMode }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isVoiceMode) "Voice Mode" else "Text Mode",
                    color = SoftBluePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // AI Interviewer Avatar
            AiInterviewerAvatar(
                isSpeaking = currentStep == InterviewStep.QUESTION_ACTIVE,
                isListening = isListening,
                isThinking = currentStep == InterviewStep.EVALUATING
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card
            currentQuestion?.let { q ->
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isHandlingFollowUp) AiPurpleContainer else SoftBluePrimary.copy(alpha = 0.12f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isHandlingFollowUp) "DYNAMIC FOLLOW-UP" else "${q.category.uppercase()} QUESTION",
                                    color = if (isHandlingFollowUp) AiPurple else SoftBluePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    val textToRead = if (isHandlingFollowUp && currentFollowUpQuestion != null) {
                                        currentFollowUpQuestion!!
                                    } else q.questionText
                                    speechService.speak(textToRead)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Replay Question",
                                    tint = SoftBluePrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isHandlingFollowUp && currentFollowUpQuestion != null) {
                                currentFollowUpQuestion!!
                            } else q.questionText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary,
                                lineHeight = 28.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Answering / Input Area
            when (currentStep) {
                InterviewStep.QUESTION_ACTIVE, InterviewStep.ANSWERING, InterviewStep.FOLLOW_UP_ACTIVE -> {
                    // Candidate Answer Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isVoiceMode) "Speak your response clearly" else "Type your response",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )

                            if (isVoiceMode) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Waveform
                                AnimatedWaveform(
                                    isListening = isListening,
                                    soundLevel = soundLevel
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Mic Button
                                Surface(
                                    onClick = {
                                        if (isListening) {
                                            speechService.stopListening()
                                        } else {
                                            val hasPerm = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) {
                                                speechService.startListening { recognized ->
                                                    answerText = recognized
                                                }
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    color = if (isListening) ErrorRed else SoftBluePrimary,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .testTag("mic_toggle_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                if (speechError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = speechError!!,
                                        style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Transcript / Text Input Field
                            OutlinedTextField(
                                value = answerText,
                                onValueChange = { answerText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("candidate_answer_textfield"),
                                placeholder = {
                                    Text(
                                        if (isVoiceMode) "Speech transcript will appear here. You can also edit it manually..." else "Write your answer here...",
                                        fontSize = 13.sp,
                                        color = CharcoalTextMuted
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftBluePrimary,
                                    unfocusedBorderColor = OutlineBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Answer Button
                            Button(
                                onClick = {
                                    if (answerText.isNotBlank() && currentQuestion != null) {
                                        currentStep = InterviewStep.EVALUATING
                                        speechService.stopListening()
                                        scope.launch {
                                            val profile = repository.getCandidateProfileSync()
                                            val feedback = geminiService.evaluateAnswer(
                                                question = if (isHandlingFollowUp && currentFollowUpQuestion != null) {
                                                    currentFollowUpQuestion!!
                                                } else currentQuestion.questionText,
                                                answer = answerText,
                                                category = currentQuestion.category,
                                                candidateProfile = profile
                                            )
                                            currentFeedback = feedback
                                            evaluatedAnswers.add(Pair(currentQuestion.questionText, feedback))

                                            // Save answer in Room
                                            repository.saveAnswer(
                                                InterviewAnswer(
                                                    sessionId = sessionId,
                                                    questionId = currentQuestion.id,
                                                    questionText = currentQuestion.questionText,
                                                    transcript = answerText,
                                                    inputSource = if (isVoiceMode) "VOICE" else "TEXT",
                                                    overallScore = feedback.overallScore,
                                                    relevanceScore = feedback.relevance,
                                                    technicalScore = feedback.technicalAccuracy,
                                                    clarityScore = feedback.clarity,
                                                    completenessScore = feedback.completeness,
                                                    structureScore = feedback.structure,
                                                    communicationScore = feedback.communication
                                                )
                                            )

                                            currentStep = InterviewStep.EVALUATION_RESULT
                                        }
                                    }
                                },
                                enabled = answerText.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_answer_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                            ) {
                                Text(
                                    text = "Submit Answer for Evaluation",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                InterviewStep.EVALUATING -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = SoftBluePrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Evaluating Your Answer...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Analyzing technical accuracy, structure, and evidence metrics.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CharcoalTextSecondary
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                InterviewStep.EVALUATION_RESULT -> {
                    currentFeedback?.let { feedback ->
                        // Real-time evaluation breakdown
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
                                            text = "Answer Evaluation",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Instant AI Rubric Breakdown",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = CharcoalTextSecondary
                                            )
                                        )
                                    }
                                    ScoreBadge(score = feedback.overallScore, suffix = "/100")
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick rubric chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MetricPill(label = "Relevance", score = feedback.relevance, modifier = Modifier.weight(1f))
                                    MetricPill(label = "Technical", score = feedback.technicalAccuracy, modifier = Modifier.weight(1f))
                                    MetricPill(label = "Clarity", score = feedback.clarity, modifier = Modifier.weight(1f))
                                    MetricPill(label = "Evidence", score = feedback.evidence, modifier = Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Strengths
                                if (feedback.strengths.isNotEmpty()) {
                                    Text(
                                        text = "Strengths",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    feedback.strengths.forEach { s ->
                                        Text(
                                            text = "• $s",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = CharcoalTextSecondary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Improvements
                                if (feedback.improvements.isNotEmpty()) {
                                    Text(
                                        text = "Improvements",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    feedback.improvements.forEach { imp ->
                                        Text(
                                            text = "• $imp",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = CharcoalTextSecondary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // STAR feedback if behavioral
                                feedback.starFeedback?.let { star ->
                                    StarFeedbackCard(star = star)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Action Buttons: Follow-up question OR next primary question
                                if (feedback.followUpQuestion != null && !isHandlingFollowUp) {
                                    Button(
                                        onClick = {
                                            currentFollowUpQuestion = feedback.followUpQuestion
                                            isHandlingFollowUp = true
                                            answerText = ""
                                            currentStep = InterviewStep.QUESTION_ACTIVE
                                            speechService.speak(feedback.followUpQuestion!!)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("answer_follow_up_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AiPurple)
                                    ) {
                                        Text(
                                            text = "Answer Dynamic Follow-up",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < questions.size - 1) {
                                            currentQuestionIndex++
                                            isHandlingFollowUp = false
                                            currentFollowUpQuestion = null
                                            answerText = ""
                                            currentStep = InterviewStep.QUESTION_ACTIVE
                                            questions.getOrNull(currentQuestionIndex)?.let { nextQ ->
                                                speechService.speak(nextQ.questionText)
                                            }
                                        } else {
                                            // Complete session
                                            scope.launch {
                                                val rep = geminiService.generateInterviewReport(
                                                    jobTitle = session?.jobTitle ?: "AI/ML Engineer",
                                                    sessionAnswers = evaluatedAnswers
                                                )
                                                session?.let { s ->
                                                    repository.updateSession(
                                                        s.copy(
                                                            completedAt = System.currentTimeMillis(),
                                                            overallScore = rep.overallScore,
                                                            technicalScore = rep.technicalScore,
                                                            communicationScore = rep.communicationScore,
                                                            structureScore = rep.structureScore,
                                                            confidenceScore = rep.confidenceScore,
                                                            relevanceScore = rep.relevanceScore,
                                                            status = "COMPLETED"
                                                        )
                                                    )
                                                }
                                                onFinishInterview(sessionId)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("next_question_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary)
                                ) {
                                    Text(
                                        text = if (currentQuestionIndex < questions.size - 1) "Next Question" else "Complete & View Report",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun MetricPill(label: String, score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WarmBackground)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = CharcoalTextSecondary
            )
        }
    }
}
