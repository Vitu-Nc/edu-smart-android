package com.example.edu_smart.quiz

import android.content.Context
import com.example.edu_smart.models.*
import org.json.JSONArray

class FactBankSource(
    private val context: Context,
    private val subject: Subject,
    private val assetPath: String
) : QuestionSource {

    override fun generate(count: Int, difficulty: Difficulty): List<QuizQuestion> {
        val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val arr = JSONArray(raw)
        val all = (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val q = obj.getString("question")
            val optionsJson = obj.getJSONArray("options")
            val options = (0 until optionsJson.length()).map { optionsJson.getString(it) }
            val idx = obj.getInt("correctIndex")
            val expl = if (obj.has("explanation")) obj.optString("explanation", null) else null
            QuizQuestion(
                id = "$subject-$i",
                subject = subject,
                question = q,
                options = options,
                correctAnswerIndex = idx,
                explanation = expl
            )
        }
        return all.shuffled().take(count)
    }
}
