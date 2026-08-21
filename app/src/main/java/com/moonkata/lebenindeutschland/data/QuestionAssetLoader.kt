package com.moonkata.lebenindeutschland.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Parses the bundled question JSON files under assets/questions (see SOURCE.md) into [Question] rows. */
object QuestionAssetLoader {
    fun loadAll(context: Context): List<Question> {
        val questions = mutableListOf<Question>()
        questions += loadFile(context, "questions/general.json")
        for (code in Topics.bundeslandNames.keys) {
            questions += loadFile(context, "questions/bundesland_$code.json")
        }
        return questions
    }

    private fun loadFile(context: Context, assetPath: String): List<Question> {
        val text = context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONArray(text)
        return (0 until array.length()).map { i -> array.getJSONObject(i).toQuestion() }
    }

    private fun JSONObject.toQuestion(): Question = Question(
        id = getInt("id"),
        category = QuestionCategory.valueOf(getString("category")),
        bundesland = optStringOrNull("bundesland"),
        topicId = getString("topicId"),
        textDe = getString("textDe"),
        answerA = getString("answerA"),
        answerB = getString("answerB"),
        answerC = getString("answerC"),
        answerD = getString("answerD"),
        correctAnswerIndex = getInt("correctAnswerIndex"),
        explanationDe = optStringOrNull("explanationDe"),
        imageAsset = optStringOrNull("imageAsset"),
        imageCaption = optStringOrNull("imageCaption"),
    )

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}
