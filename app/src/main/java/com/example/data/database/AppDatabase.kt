package com.example.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.models.CandidateProfile
import com.example.data.models.InterviewAnswer
import com.example.data.models.InterviewQuestion
import com.example.data.models.InterviewSession
import com.example.data.models.JobProfile
import com.example.data.models.PracticePlan
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDao {
    @Query("SELECT * FROM candidate_profile LIMIT 1")
    fun getCandidateProfile(): Flow<CandidateProfile?>

    @Query("SELECT * FROM candidate_profile LIMIT 1")
    suspend fun getCandidateProfileSync(): CandidateProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCandidateProfile(profile: CandidateProfile)

    @Query("DELETE FROM candidate_profile")
    suspend fun clearCandidateProfile()
}

@Dao
interface JobDao {
    @Query("SELECT * FROM job_profiles ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobProfile>>

    @Query("SELECT * FROM job_profiles WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobProfile)

    @Query("DELETE FROM job_profiles WHERE id = :id")
    suspend fun deleteJobById(id: String)

    @Query("DELETE FROM job_profiles")
    suspend fun clearAllJobs()
}

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interview_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interview_sessions WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedSessions(): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interview_sessions ORDER BY startedAt DESC LIMIT 5")
    fun getRecentSessions(): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interview_sessions WHERE id = :id LIMIT 1")
    fun getSessionFlow(id: String): Flow<InterviewSession?>

    @Query("SELECT * FROM interview_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionSync(id: String): InterviewSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSession)

    @Update
    suspend fun updateSession(session: InterviewSession)

    @Query("DELETE FROM interview_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM interview_sessions")
    suspend fun clearAllSessions()

    // Questions
    @Query("SELECT * FROM interview_questions WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getQuestionsForSession(sessionId: String): Flow<List<InterviewQuestion>>

    @Query("SELECT * FROM interview_questions WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getQuestionsForSessionSync(sessionId: String): List<InterviewQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: InterviewQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<InterviewQuestion>)

    // Answers
    @Query("SELECT * FROM interview_answers WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getAnswersForSession(sessionId: String): Flow<List<InterviewAnswer>>

    @Query("SELECT * FROM interview_answers WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getAnswersForSessionSync(sessionId: String): List<InterviewAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: InterviewAnswer)

    @Query("DELETE FROM interview_answers WHERE sessionId = :sessionId")
    suspend fun deleteAnswersForSession(sessionId: String)

    @Query("DELETE FROM interview_answers")
    suspend fun clearAllAnswers()
}

@Dao
interface PracticePlanDao {
    @Query("SELECT * FROM practice_plans WHERE date = :date LIMIT 1")
    fun getPlanForDate(date: String): Flow<PracticePlan?>

    @Query("SELECT * FROM practice_plans WHERE date = :date LIMIT 1")
    suspend fun getPlanForDateSync(date: String): PracticePlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: PracticePlan)

    @Query("DELETE FROM practice_plans")
    suspend fun clearAllPlans()
}

@Database(
    entities = [
        CandidateProfile::class,
        JobProfile::class,
        InterviewSession::class,
        InterviewQuestion::class,
        InterviewAnswer::class,
        PracticePlan::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun candidateDao(): CandidateDao
    abstract fun jobDao(): JobDao
    abstract fun interviewDao(): InterviewDao
    abstract fun practicePlanDao(): PracticePlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interviewiq_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
