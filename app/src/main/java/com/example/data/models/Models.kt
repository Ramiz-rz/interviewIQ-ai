package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "candidate_profile")
data class CandidateProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val targetRole: String = "AI/ML Engineer",
    val experienceLevel: String = "Senior",
    val summary: String = "",
    val skills: String = "", // Comma-separated
    val experience: String = "",
    val education: String = "",
    val projects: String = "",
    val certifications: String = "",
    val achievements: String = "",
    val resumePath: String? = null,
    val resumeRawText: String = "",
    val strengthScore: Int = 0,
    val analysisJson: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_profiles")
data class JobProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val company: String = "",
    val seniority: String = "Mid-Senior",
    val description: String,
    val requiredSkills: String = "", // Comma-separated
    val preferredSkills: String = "",
    val responsibilities: String = "",
    val domain: String = "Technology",
    val interviewTopics: String = "",
    val matchScore: Int = 0,
    val gapAnalysisJson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_sessions")
data class InterviewSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val jobProfileId: String? = null,
    val jobTitle: String = "AI/ML Engineer",
    val type: String = "Technical", // Technical, Behavioral, HR, Mixed
    val difficulty: String = "Advanced", // Beginner, Intermediate, Advanced, Expert
    val durationMinutes: Int = 45,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val overallScore: Int = 0,
    val technicalScore: Int = 0,
    val communicationScore: Int = 0,
    val structureScore: Int = 0,
    val confidenceScore: Int = 0,
    val relevanceScore: Int = 0,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, ABANDONED
    val reportJson: String = ""
)

@Entity(tableName = "interview_questions")
data class InterviewQuestion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val questionText: String,
    val category: String = "Technical", // Technical, Behavioral, HR
    val difficulty: String = "Advanced",
    val orderIndex: Int = 0,
    val isFollowUp: Boolean = false,
    val parentQuestionId: String? = null
)

@Entity(tableName = "interview_answers")
data class InterviewAnswer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val questionId: String,
    val questionText: String,
    val transcript: String,
    val inputSource: String = "VOICE", // VOICE, TEXT
    val overallScore: Int = 0,
    val relevanceScore: Int = 0,
    val technicalScore: Int = 0,
    val clarityScore: Int = 0,
    val completenessScore: Int = 0,
    val structureScore: Int = 0,
    val communicationScore: Int = 0,
    val feedbackJson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "practice_plans")
data class PracticePlan(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val focusArea: String = "Answer Structure",
    val technicalTargetCount: Int = 2,
    val technicalCompletedCount: Int = 0,
    val behavioralTargetCount: Int = 1,
    val behavioralCompletedCount: Int = 0,
    val weakAreaReviewed: Boolean = false,
    val completed: Boolean = false
)

// UI and Analysis helper models (non-persisted directly or serializable to JSON)

data class ResumeStrengthAnalysis(
    val score: Int = 82,
    val skillsClarity: String = "Strong technical terminology and frameworks highlighted.",
    val experienceQuality: String = "Demonstrated hands-on engineering experience in machine learning pipelines.",
    val projectDescriptions: String = "Projects show end-to-end implementation from data collection to deployment.",
    val quantifiableAchievements: String = "Needs more quantifiable impact metrics (e.g., % latency reduction, throughput).",
    val technicalDepth: String = "High depth in RAG architectures and Python ecosystem.",
    val keywords: List<String> = listOf("Python", "RAG", "FAISS", "LangChain", "PyTorch", "Docker"),
    val missingInformation: List<String> = listOf("Production monitoring metrics", "Cloud deployment scaling"),
    val formattingConcerns: String = "Clean layout. Ensure section headers are consistently formatted."
)

data class SkillGapAnalysis(
    val matchPercentage: Int = 78,
    val strongMatches: List<SkillMatchItem> = emptyList(),
    val skillsToStrengthen: List<SkillMatchItem> = emptyList(),
    val missingOrLowEvidence: List<SkillMatchItem> = emptyList(),
    val summaryAdvice: String = ""
)

data class JobAnalysisResult(
    val title: String = "AI/ML Engineer",
    val company: String = "Tech Enterprise",
    val seniority: String = "Senior",
    val domain: String = "Artificial Intelligence",
    val requiredSkills: List<String> = emptyList(),
    val preferredSkills: List<String> = emptyList(),
    val responsibilities: String = "",
    val interviewTopics: List<String> = emptyList(),
    val matchPercentage: Int = 78,
    val strongMatches: List<SkillMatchItem> = emptyList(),
    val skillsToStrengthen: List<SkillMatchItem> = emptyList(),
    val missingOrLowEvidence: List<SkillMatchItem> = emptyList(),
    val summaryAdvice: String = ""
)

data class SkillMatchItem(
    val skill: String,
    val reason: String
)

data class AnswerFeedback(
    val overallScore: Int = 80,
    val relevance: Int = 85,
    val technicalAccuracy: Int = 82,
    val completeness: Int = 78,
    val clarity: Int = 80,
    val structure: Int = 75,
    val communication: Int = 80,
    val evidence: Int = 76,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val betterStructureAdvice: String = "",
    val missedKeyPoints: List<String> = emptyList(),
    val starFeedback: StarFeedback? = null,
    val followUpQuestion: String? = null
)

data class StarFeedback(
    val situation: String = "Strong",
    val task: String = "Good",
    val action: String = "Detailed",
    val result: String = "Needs quantifiable metrics"
)

data class FinalInterviewReport(
    val overallScore: Int = 82,
    val technicalScore: Int = 86,
    val communicationScore: Int = 78,
    val structureScore: Int = 80,
    val confidenceScore: Int = 74,
    val relevanceScore: Int = 88,
    val strongestAreas: List<String> = emptyList(),
    val areasToImprove: List<String> = emptyList(),
    val recommendedPractice: List<String> = emptyList(),
    val summaryFeedback: String = ""
)

data class PracticeQuestion(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val category: String,
    val difficulty: String,
    val sampleAnswerHint: String
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
