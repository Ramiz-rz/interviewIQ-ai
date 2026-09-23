package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.database.AppDatabase
import com.example.data.repositories.InterviewRepository
import com.example.data.services.BillingService
import com.example.data.services.GeminiService
import com.example.data.services.SpeechService
import com.example.ui.navigation.Screen
import com.example.ui.screens.career_coach.CareerCoachScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.interview.InterviewReportScreen
import com.example.ui.screens.interview.InterviewSessionScreen
import com.example.ui.screens.interview.InterviewSetupScreen
import com.example.ui.screens.jobs.JobScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.resume.ResumeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.CharcoalTextPrimary
import com.example.ui.theme.CharcoalTextSecondary
import com.example.ui.theme.InterviewIQTheme
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SoftBluePrimary
import com.example.ui.theme.WarmBackground

class MainActivity : ComponentActivity() {

    private lateinit var speechService: SpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = InterviewRepository(database)
        val geminiService = GeminiService()
        val billingService = BillingService(this)
        speechService = SpeechService(this)

        setContent {
            InterviewIQTheme {
                MainApp(
                    repository = repository,
                    geminiService = geminiService,
                    billingService = billingService,
                    speechService = speechService
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService.destroy()
    }
}

@Composable
fun MainApp(
    repository: InterviewRepository,
    geminiService: GeminiService,
    billingService: BillingService,
    speechService: SpeechService
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("interviewiq_prefs", Context.MODE_PRIVATE) }
    val onboardingCompleted = remember { prefs.getBoolean("onboarding_completed", false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on Onboarding and during an active interview session
    val showBottomBar = currentRoute != Screen.Onboarding.route &&
        currentRoute?.startsWith("interview_session/") != true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        prefs.edit().putBoolean("onboarding_completed", true).apply()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onStartInterviewClick = { jobTitle ->
                        navController.navigate(Screen.Interviews.route)
                    },
                    onNavigateToResume = { navController.navigate(Screen.Resume.route) },
                    onNavigateToJob = { navController.navigate(Screen.Job.route) },
                    onNavigateToPractice = { navController.navigate(Screen.Practice.route) },
                    onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                    onNavigateToSessionReport = { sessionId ->
                        navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                    }
                )
            }

            // Resume Analyzer
            composable(Screen.Resume.route) {
                ResumeScreen(
                    repository = repository,
                    geminiService = geminiService,
                    onBack = { navController.popBackStack() }
                )
            }

            // Job Analyzer
            composable(Screen.Job.route) {
                JobScreen(
                    repository = repository,
                    geminiService = geminiService,
                    onBack = { navController.popBackStack() },
                    onStartInterviewForJob = { jobTitle ->
                        navController.navigate(Screen.Interviews.route)
                    }
                )
            }

            // Interviews Hub
            composable(Screen.Interviews.route) {
                InterviewSetupScreen(
                    repository = repository,
                    geminiService = geminiService,
                    billingService = billingService,
                    onStartSession = { sessionId ->
                        navController.navigate(Screen.InterviewSession.createRoute(sessionId))
                    },
                    onViewReport = { sessionId ->
                        navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                    }
                )
            }

            // Live Interview Session
            composable(
                route = Screen.InterviewSession.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                InterviewSessionScreen(
                    sessionId = sessionId,
                    repository = repository,
                    geminiService = geminiService,
                    speechService = speechService,
                    onBack = { navController.popBackStack() },
                    onFinishInterview = { finishedSessionId ->
                        navController.navigate(Screen.InterviewReport.createRoute(finishedSessionId)) {
                            popUpTo(Screen.Interviews.route)
                        }
                    }
                )
            }

            // Performance Report
            composable(
                route = Screen.InterviewReport.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                InterviewReportScreen(
                    sessionId = sessionId,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onStartPractice = { navController.navigate(Screen.Practice.route) }
                )
            }

            // Practice
            composable(Screen.Practice.route) {
                PracticeScreen(
                    repository = repository,
                    geminiService = geminiService,
                    onNavigateToCareerCoach = { navController.navigate(Screen.CareerCoach.route) }
                )
            }

            // Career Coach Chat
            composable(Screen.CareerCoach.route) {
                CareerCoachScreen(
                    repository = repository,
                    geminiService = geminiService,
                    onBack = { navController.popBackStack() }
                )
            }

            // Progress & Analytics
            composable(Screen.Progress.route) {
                ProgressScreen(
                    repository = repository,
                    onNavigateToSessionReport = { sessionId ->
                        navController.navigate(Screen.InterviewReport.createRoute(sessionId))
                    },
                    onStartPractice = { navController.navigate(Screen.Practice.route) }
                )
            }

            // Settings & Billing
            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    billingService = billingService
                )
            }
        }
    }
}

data class BottomNavTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavTab(Screen.Home.route, "Home", Icons.Default.Home, "bottom_nav_home"),
        BottomNavTab(Screen.Interviews.route, "Interviews", Icons.Default.RecordVoiceOver, "bottom_nav_interviews"),
        BottomNavTab(Screen.Practice.route, "Practice", Icons.Default.Quiz, "bottom_nav_practice"),
        BottomNavTab(Screen.Progress.route, "Progress", Icons.Default.BarChart, "bottom_nav_progress"),
        BottomNavTab(Screen.Settings.route, "Settings", Icons.Default.Settings, "bottom_nav_settings")
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, OutlineBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        items.forEach { tab ->
            val selected = currentRoute == tab.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoftBluePrimary,
                    selectedTextColor = SoftBluePrimary,
                    unselectedIconColor = CharcoalTextSecondary,
                    unselectedTextColor = CharcoalTextMuted,
                    indicatorColor = PrimaryContainer
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
