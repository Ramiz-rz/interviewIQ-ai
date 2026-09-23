package com.example

import com.example.ai.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiServiceTest {

    private lateinit var geminiService: GeminiService

    @Before
    fun setUp() {
        geminiService = GeminiService(apiKey = "")
    }

    @Test
    fun analyzeResume_returnsValidAnalysis() = runBlocking {
        val resumeSample = """
            Senior Android Engineer with 6 years experience in Kotlin, Jetpack Compose, Room, and Coroutines.
            Architected offline-first applications and reduced app startup latency by 35%.
        """.trimIndent()

        val result = geminiService.analyzeResume(resumeSample, "Senior Android Engineer")
        assertNotNull(result)
        assertTrue(result.score > 0)
        assertTrue(result.skillsClarity.isNotBlank())
        assertTrue(result.keywords.isNotEmpty())
    }

    @Test
    fun evaluateAnswer_returnsStructuredFeedback() = runBlocking {
        val question = "How do you avoid memory leaks with Kotlin Coroutines?"
        val answer = "By using viewModelScope and structured concurrency which cancels jobs when the ViewModel clears."

        val feedback = geminiService.evaluateAnswer(question, answer, "Android Engineer", "Technical")
        assertNotNull(feedback)
        assertTrue(feedback.overallScore in 1..100)
        assertTrue(feedback.relevance > 0)
        assertTrue(feedback.strengths.isNotEmpty())
    }

    @Test
    fun generateInterview_returnsRequestedQuestionCount() = runBlocking {
        val questions = geminiService.generateInterview("Mobile Architect", "Technical", "Advanced", 3)
        assertNotNull(questions)
        assertEquals(3, questions.size)
        assertTrue(questions.all { it.questionText.isNotBlank() })
    }
}
