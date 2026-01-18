package com.example.edu_smart.models

data class QuizQuestion(
    val id: String,
    val subject: Subject,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,         // 👈 add this
    val difficulty: Difficulty = Difficulty.MEDIUM
)

enum class Subject { MALAWI_HISTORY, BIOLOGY, MATHS, AGRICULTURE, RANDOM }
enum class Difficulty { EASY, MEDIUM, HARD }

