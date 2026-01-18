package com.example.edu_smart.quiz

import com.example.edu_smart.models.Difficulty
import com.example.edu_smart.models.QuizQuestion

interface QuestionSource {
    fun generate(count: Int, difficulty: Difficulty): List<QuizQuestion>
}
