package com.dec.attendpro.ui.models

data class ClassInfo(
    val subject: String,
    val time: String,
    val room: String,
    val studentCount: Int
)

data class StudentBrief(
    val name: String, 
    val id: String,
    val photoUrl: String? = null
)

data class AttendanceRecord(
    val subject: String,
    val date: String,
    val isPresent: Boolean
)

val mockTodayClasses = listOf(
    ClassInfo("Computer Science 101", "09:00 AM - 10:00 AM", "Room 402", 45),
    ClassInfo("Machine Learning", "11:30 AM - 01:00 PM", "Lab 2", 38),
    ClassInfo("Mobile App Dev", "02:00 PM - 03:30 PM", "Room 105", 42)
)

val mockStudentList = listOf(
    StudentBrief("Robert Fox", "ID: 202101"),
    StudentBrief("Jane Cooper", "ID: 202102"),
    StudentBrief("Guy Hawkins", "ID: 202103"),
    StudentBrief("Arlene McCoy", "ID: 202104"),
    StudentBrief("Bessie Cooper", "ID: 202105"),
    StudentBrief("Cody Fisher", "ID: 202106")
)

val mockAttendanceHistory = listOf(
    AttendanceRecord("Computer Science 101", "Oct 24, 2023", true),
    AttendanceRecord("Machine Learning", "Oct 24, 2023", true),
    AttendanceRecord("Mobile App Dev", "Oct 23, 2023", false),
    AttendanceRecord("Computer Science 101", "Oct 23, 2023", true),
    AttendanceRecord("English Literature", "Oct 22, 2023", true)
)
