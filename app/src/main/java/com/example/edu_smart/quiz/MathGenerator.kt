package com.example.edu_smart.quiz

import com.example.edu_smart.models.*
import kotlin.random.Random
import kotlin.math.round

class MathGenerator : QuestionSource {

    override fun generate(count: Int, difficulty: Difficulty): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        repeat(count) { index ->
            // Randomly choose what kind of math question to make
            when (Random.nextInt(3)) {
                0 -> questions += makeArithmetic(index, difficulty)
                1 -> questions += makePercentage(index, difficulty)
                else -> questions += makeAlgebra(index, difficulty)
            }
        }
        return questions
    }

    private fun rangeFor(diff: Difficulty): IntRange = when (diff) {
        Difficulty.EASY -> 1..20
        Difficulty.MEDIUM -> 10..60
        Difficulty.HARD -> 25..150
    }

    private fun makeArithmetic(id: Int, diff: Difficulty): QuizQuestion {
        val a = rangeFor(diff).random()
        val b = rangeFor(diff).random()
        val op = listOf("+", "-", "×", "÷").random()

        val (text, correct) = when (op) {
            "+" -> "What is $a + $b?" to (a + b).toDouble()
            "-" -> "What is $a - $b?" to (a - b).toDouble()
            "×" -> "What is $a × $b?" to (a * b).toDouble()
            "÷" -> {
                val prod = a * b
                "What is $prod ÷ $a?" to b.toDouble()
            }
            else -> "?" to 0.0
        }

        val options = buildOptions(correct)
        return QuizQuestion(
            id = "MATH-$id",
            subject = Subject.MATHS,
            question = text,
            options = options,
            correctAnswerIndex = options.indexOf(correct.toInt().toString()),
            explanation = "Basic arithmetic: result = $correct",
            difficulty = diff
        )
    }

    private fun makePercentage(id: Int, diff: Difficulty): QuizQuestion {
        val base = rangeFor(diff).random()
        val percent = listOf(5, 10, 12, 15, 20, 25, 30).random()
        val inc = Random.nextBoolean()
        val newVal = if (inc) base * (1 + percent / 100.0) else base * (1 - percent / 100.0)
        val correct = round(newVal)
        val text = "A value is ${if (inc) "increased" else "decreased"} by $percent%. " +
                "If the original was $base, what is the new value (nearest whole number)?"
        val options = buildOptions(correct)
        return QuizQuestion(
            id = "MATH-P$id",
            subject = Subject.MATHS,
            question = text,
            options = options,
            correctAnswerIndex = options.indexOf(correct.toInt().toString()),
            explanation = "New = $base × (1 ± $percent/100) = $correct",
            difficulty = diff
        )
    }

    private fun makeAlgebra(id: Int, diff: Difficulty): QuizQuestion {
        val a = rangeFor(diff).random().coerceAtLeast(1)
        val x = rangeFor(diff).random()
        val b = rangeFor(diff).random()
        val y = a * x + b
        val text = "Solve for x: $a·x + $b = $y"
        val correct = x.toDouble()
        val options = buildOptions(correct)
        return QuizQuestion(
            id = "MATH-A$id",
            subject = Subject.MATHS,
            question = text,
            options = options,
            correctAnswerIndex = options.indexOf(correct.toInt().toString()),
            explanation = "x = (y - b)/a = (${y} - $b)/$a = $x",
            difficulty = diff
        )
    }

    private fun buildOptions(correct: Double): List<String> {
        val wrongs = mutableSetOf<Double>()
        while (wrongs.size < 3) {
            val noise = Random.nextInt(-10, 10)
            val cand = correct + noise
            if (cand != correct) wrongs += cand
        }
        return (wrongs.map { it.toInt().toString() } + correct.toInt().toString()).shuffled()
    }
}
