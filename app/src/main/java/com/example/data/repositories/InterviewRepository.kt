package com.example.data.repositories

import com.example.data.database.AppDatabase
import com.example.data.models.CandidateProfile
import com.example.data.models.InterviewAnswer
import com.example.data.models.InterviewQuestion
import com.example.data.models.InterviewSession
import com.example.data.models.JobProfile
import com.example.data.models.PracticePlan
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InterviewRepository(private val database: AppDatabase) {

    private val interviewDao = database.interviewDao()
    private val candidateDao = database.candidateDao()
    private val jobDao = database.jobDao()
    private val practicePlanDao = database.practicePlanDao()

    // Candidate
    val candidateProfile: Flow<CandidateProfile?> = candidateDao.getCandidateProfile()
    suspend fun getCandidateProfileSync(): CandidateProfile? = candidateDao.getCandidateProfileSync()
    suspend fun saveCandidateProfile(profile: CandidateProfile) = candidateDao.saveCandidateProfile(profile)
    suspend fun clearCandidateProfile() = candidateDao.clearCandidateProfile()

    // Jobs
    val allJobs: Flow<List<JobProfile>> = jobDao.getAllJobs()
    suspend fun getJobById(id: String): JobProfile? = jobDao.getJobById(id)
    suspend fun saveJob(job: JobProfile) = jobDao.insertJob(job)
    suspend fun deleteJobById(id: String) = jobDao.deleteJobById(id)
    suspend fun clearAllJobs() = jobDao.clearAllJobs()

    // Sessions
    val allSessions: Flow<List<InterviewSession>> = interviewDao.getAllSessions()
    val completedSessions: Flow<List<InterviewSession>> = interviewDao.getCompletedSessions()
    val recentSessions: Flow<List<InterviewSession>> = interviewDao.getRecentSessions()

    fun getSessionFlow(id: String): Flow<InterviewSession?> = interviewDao.getSessionFlow(id)
    suspend fun getSessionSync(id: String): InterviewSession? = interviewDao.getSessionSync(id)
    suspend fun createSession(session: InterviewSession) = interviewDao.insertSession(session)
    suspend fun updateSession(session: InterviewSession) = interviewDao.updateSession(session)
    suspend fun deleteSession(id: String) = interviewDao.deleteSessionById(id)
    suspend fun clearAllSessions() {
        interviewDao.clearAllSessions()
        interviewDao.clearAllAnswers()
    }

    // Questions
    fun getQuestionsForSession(sessionId: String): Flow<List<InterviewQuestion>> =
        interviewDao.getQuestionsForSession(sessionId)
    suspend fun getQuestionsForSessionSync(sessionId: String): List<InterviewQuestion> =
        interviewDao.getQuestionsForSessionSync(sessionId)
    suspend fun addQuestion(question: InterviewQuestion) = interviewDao.insertQuestion(question)
    suspend fun addQuestions(questions: List<InterviewQuestion>) = interviewDao.insertQuestions(questions)

    // Answers
    fun getAnswersForSession(sessionId: String): Flow<List<InterviewAnswer>> =
        interviewDao.getAnswersForSession(sessionId)
    suspend fun getAnswersForSessionSync(sessionId: String): List<InterviewAnswer> =
        interviewDao.getAnswersForSessionSync(sessionId)
    suspend fun saveAnswer(answer: InterviewAnswer) = interviewDao.insertAnswer(answer)

    // Practice Plan
    fun getTodayPracticePlan(): Flow<PracticePlan?> {
        val todayStr = try {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            "2026-09-23"
        }
        return practicePlanDao.getPlanForDate(todayStr)
    }

    suspend fun getOrCreateTodayPracticePlan(): PracticePlan {
        val todayStr = try {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            "2026-09-23"
        }
        val existing = practicePlanDao.getPlanForDateSync(todayStr)
        if (existing != null) return existing

        val newPlan = PracticePlan(
            date = todayStr,
            focusArea = "Answer Structure",
            technicalTargetCount = 2,
            technicalCompletedCount = 1,
            behavioralTargetCount = 1,
            behavioralCompletedCount = 0,
            weakAreaReviewed = false,
            completed = false
        )
        practicePlanDao.savePlan(newPlan)
        return newPlan
    }

    suspend fun savePracticePlan(plan: PracticePlan) = practicePlanDao.savePlan(plan)
    suspend fun clearAllPracticePlans() = practicePlanDao.clearAllPlans()

    // Privacy wipe
    suspend fun clearAllUserData() {
        candidateDao.clearCandidateProfile()
        jobDao.clearAllJobs()
        interviewDao.clearAllSessions()
        interviewDao.clearAllAnswers()
        practicePlanDao.clearAllPlans()
    }
}
