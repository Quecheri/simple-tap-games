package polsl.game.server.model

import polsl.game.server.repository.Question
import polsl.game.server.repository.QuestionRepository
import polsl.game.server.repository.SHOULD_CLICK

abstract class GameStrategy(protected val questionRepository: QuestionRepository)
{
    abstract fun getQuestion(): Question
    abstract fun getGameStateString(): String
    abstract fun isGameOver(): Boolean
    abstract fun updateScore(result: Int)
    abstract fun getScore() : Int
    open fun getProgress():Float
    {
        return 0F
    }

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
    private var initialNumberOfQuestions = 20
    private var numberOfQuestions = initialNumberOfQuestions
    private var shouldClick = false
    private var results = FastReactionResults(0,0,0,0,0)

    override fun getQuestion(): Question
    {
        val q = questionRepository.getFastReactionQuestion()
        shouldClick = q.question == SHOULD_CLICK
        return q
    }

    override fun getGameStateString(): String {
        return "Num of question left: $numberOfQuestions"
    }

    override fun isGameOver(): Boolean {
        return numberOfQuestions <= 0
    }

    override fun updateScore(result: Int) {
        numberOfQuestions--

        if(result < 0)
        {
            if(shouldClick)
            {
                results.skips++
            }
            else
            {
                results.incorrectReactions++
                results.incorrectReactionTime += result
            }
        }
        else
        {
            if(shouldClick)
            {
                results.correctReactions++
                results.correctReactionTime += result
            }
            else
            {
                results.incorrectReactions++
                results.incorrectReactionTime += result
            }

        }
    }

    override fun getScore(): Int {
        return numberOfQuestions
    }

    override fun getProgress():Float
    {
        return 1 - numberOfQuestions.toFloat() / initialNumberOfQuestions
    }


    fun getResultSting():String
    {
        val falseReactions = results.incorrectReactions + results.skips
        val nonFalseReactions = results.correctReactions
        val allReactions = falseReactions+nonFalseReactions

        val avgFalseTime = (results.incorrectReactionTime / results.incorrectReactions).toDouble()
        val avgNonFalseTime = (results.correctReactionTime / results.correctReactions).toDouble()

        return "Incorrect reactions: $falseReactions (including ${results.skips} skips) with avg time $avgFalseTime ms \nCorrect reactions: $nonFalseReactions with avg time $avgNonFalseTime ms \nFinal score $nonFalseReactions/$allReactions"
    }
}

data class FastReactionResults(
    var correctReactions :Int,
    var incorrectReactions :Int,
    var skips :Int,
    var correctReactionTime :Int,
    var incorrectReactionTime :Int,
)
