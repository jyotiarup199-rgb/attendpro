package com.dec.attendpro.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object TeacherHome : Screen("teacher_home")
    object StudentHome : Screen("student_home")
    object AttendanceCamera : Screen("attendance_camera")
    object Analytics : Screen("analytics")
    object StudentManagement : Screen("student_management")
    object Profile : Screen("profile")
    object RegisterFace : Screen("register_face")
    object AttendanceList : Screen("attendance_list")
}
