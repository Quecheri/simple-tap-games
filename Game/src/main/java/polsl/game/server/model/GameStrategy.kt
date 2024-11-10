package polsl.game.server.model

import polsl.game.server.repository.Question
import polsl.game.server.repository.QuestionRepository

abstract class GameStrategy(protected val questionRepository: QuestionRepository)
{
    abstract fun getQuestion(): Question
    abstract fun getGameStateString(): String
    abstract fun isGameOver(): Boolean
    abstract fun updateScore(result: Int) //TODO maybe result should be Int?
    abstract fun getScore() : Int

}

class NimStrategy(questionRepository: QuestionRepository) : GameStrategy(questionRepository)
{
    private var haystack: Int = 20
    override fun getQuestion(): Question
    {
        return questionRepository.getNimQuestion(haystack)
    }

    override fun getGameStateString(): String {
        return "Haystack: $haystack"
    }

    override fun isGameOver(): Boolean {
        return haystack<=0
    }

    override fun updateScore(result: Int) {
        haystack -= result
    }

    override fun getScore(): Int {
        return haystack
    }
}

class FastReactionStrategy(questionRepository: QuestionRepository) : GameStrategy(questionRepository)
{
    private val initialNumberOfQuestions = 20
    private var numberOfQuestions = initialNumberOfQuestions
    override fun getQuestion(): Question
    {
        return questionRepository.getFastReactionQuestion()
    }

    override fun getGameStateString(): String {
        return "Num of question left: <inf>"
    }

    override fun isGameOver(): Boolean {
        return numberOfQuestions <= 0
    }

    override fun updateScore(result: Int) {
        numberOfQuestions--
    }

    override fun getScore(): Int {
        return numberOfQuestions
    }

    fun getInitialNumberOfQuestions():Int
    {
        return initialNumberOfQuestions
    }
}