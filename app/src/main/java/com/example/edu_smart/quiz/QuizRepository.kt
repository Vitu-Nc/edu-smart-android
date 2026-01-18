package com.example.edu_smart.quiz

import android.content.Context
import com.example.edu_smart.models.*

class QuizRepository(private val context: Context) {

    private val math = MathGenerator()

    // Fact banks (JSON in assets/quizzes/)
    private val hist = FactBankSource(context, Subject.MALAWI_HISTORY, "quizzes/malawi_history.json")
    private val bio  = FactBankSource(context, Subject.BIOLOGY,        "quizzes/biology.json")
    private val agri = FactBankSource(context, Subject.AGRICULTURE,    "quizzes/agriculture.json")

    fun fetch(subject: Subject, count: Int, difficulty: Difficulty): List<QuizQuestion> = when (subject) {
        Subject.MATHS -> math.generate(count, difficulty)
        Subject.MALAWI_HISTORY -> hist.generate(count, difficulty)
        Subject.BIOLOGY -> bio.generate(count, difficulty)
        Subject.AGRICULTURE -> agri.generate(count, difficulty)
        Subject.RANDOM -> fetchRandom(count, difficulty)
    }

    fun fetchRandom(count: Int, difficulty: Difficulty): List<QuizQuestion> {
        // Pull from each subject, then shuffle & trim.
        val perBucket = maxOf(1, count / 4)

        val pool = buildList {
            addAll(math.generate(perBucket, difficulty))
            addAll(hist.generate(perBucket, difficulty))
            addAll(bio.generate(perBucket, difficulty))
            addAll(agri.generate(perBucket, difficulty))
        }.shuffled()

        // If count not divisible by 4, top-up from any source
        return if (pool.size >= count) pool.take(count)
        else (pool + math.generate(count - pool.size, difficulty)).shuffled().take(count)
    }
}
