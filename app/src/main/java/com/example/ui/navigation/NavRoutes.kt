package com.example.ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Onboarding : Screen("onboarding", "Onboarding")
    object Home : Screen("home", "Home")
    object Interviews : Screen("interviews", "Interviews")
    object Practice : Screen("practice", "Practice")
    object Progress : Screen("progress", "Progress")
    object Settings : Screen("settings", "Settings")

    // Sub-screens
    object Resume : Screen("resume", "Resume Analyzer")
    object Job : Screen("job", "Job Analyzer")
    object InterviewSession : Screen("interview_session/{sessionId}", "Mock Interview") {
        fun createRoute(sessionId: String) = "interview_session/$sessionId"
    }
    object InterviewReport : Screen("interview_report/{sessionId}", "Interview Report") {
        fun createRoute(sessionId: String) = "interview_report/$sessionId"
    }
    object CareerCoach : Screen("career_coach", "Career Coach")
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Interviews,
    Screen.Practice,
    Screen.Progress,
    Screen.Settings
)
