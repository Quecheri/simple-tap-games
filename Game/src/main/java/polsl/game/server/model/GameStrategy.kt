package polsl.game.server.model

import polsl.game.server.repository.Prompt
import polsl.game.server.repository.PromptRepository
import polsl.game.server.repository.SHOULD_CLICK

abstract class GameStrategy(protected val promptRepository: PromptRepository, protected val uintParam: UInt?)
{
    abstract fun getQuestion(): Prompt
    abstract fun getGameStateString(): String
    abstract fun isGameOver(): Boolean
    abstract fun updateScore(result: Int)
    abstract fun getScore() : Int
    open fun getProgress():Float
    {
        return 0F
    }

}

class NimStrategy(promptRepository: PromptRepository,
                  uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{
    private var haystack: Int = uintParam?.toInt() ?: 20
    override fun getQuestion(): Prompt
    {
        return promptRepository.getNimPrompt(haystack)
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

class FastReactionStrategy(promptRepository: PromptRepository,
                           uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{

    private var initialNumberOfQuestions :Int = uintParam?.toInt() ?: 20
    private var numberOfQuestions = initialNumberOfQuestions
    private var shouldClick = false
    private var results = FastReactionResults(0,0,0,0,0)

    override fun getQuestion(): Prompt
    {
        val q = promptRepository.getFastReactionPrompt()
        shouldClick = q.prompt == SHOULD_CLICK
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
                results.correctReactions++
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

        val avgFalseTime = if(falseReactions>0) (results.incorrectReactionTime / results.incorrectReactions).toDouble() else 0.0
        val avgNonFalseTime = if(nonFalseReactions>0) (results.correctReactionTime / results.correctReactions).toDouble() else 0.0

        val skipsStr = if(results.skips > 0) "(including ${results.skips} skips) " else " "


        return """Incorrect reactions: $falseReactions ${skipsStr}with avg time $avgFalseTime ms
Correct reactions: $nonFalseReactions with avg time $avgNonFalseTime ms
Final score $nonFalseReactions/$allReactions"""
    }
}

data class FastReactionResults(
    var correctReactions :Int,
    var incorrectReactions :Int,
    var skips :Int,
    var correctReactionTime :Int,
    var incorrectReactionTime :Int,
)
