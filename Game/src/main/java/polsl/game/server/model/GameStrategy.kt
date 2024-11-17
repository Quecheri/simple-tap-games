package polsl.game.server.model

import android.util.Log
import polsl.game.server.repository.Prompt
import polsl.game.server.repository.PromptRepository
import polsl.game.server.repository.SHOULD_CLICK

abstract class GameStrategy(protected val promptRepository: PromptRepository, protected val uintParam: UInt?)
{
    abstract fun getPrompt(): Prompt
    abstract fun getGameStateString(): String
    abstract fun isGameOver(): Boolean
    abstract fun updateScore(result: Int)
    abstract fun getScore() : Int
    abstract fun rollPointer(param: Int = 0) : Int
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
    private var rollingPointer: Int = -1
    override fun getPrompt(): Prompt
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
    override fun rollPointer(param: Int): Int
    {
        rollingPointer++
        if(rollingPointer==param) {
            rollingPointer = -1
        }
        return rollingPointer;
    }
}

class FastReactionStrategy(promptRepository: PromptRepository,
                           uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{

    private var initialNumberOfQuestions :Int = uintParam?.toInt() ?: 20
    private var numberOfQuestions = initialNumberOfQuestions
    private var shouldClick = false
    private var rollingPointer: Int = 0
    private var results = FastReactionResults(0,0,0,0,0)

    override fun getPrompt(): Prompt
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
    override fun rollPointer(param: Int): Int //TODO IMPLEMENT PROPERLY
    {
        rollingPointer++
        if(rollingPointer==param) {
            rollingPointer = -1
        }
        return rollingPointer;
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
class CombinationStrategy(promptRepository: PromptRepository,
                  uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{
    private var maxCombinationLength: Int = uintParam?.toInt() ?: 20
    private var currentCombinationLength: Int = 0
    private var combinationFailed = false
    private var shouldClick = false
    private var setup = true
    private var rollingPointer: Int = 0
    private var current: Int = 0
    private var responseQueue: MutableList<Int> = mutableListOf()

    override fun getPrompt(): Prompt
    {
        return promptRepository.getCombinationPrompt(setup)
    }
    fun getSetup():Boolean
    {
        return setup
    }
    override fun getGameStateString(): String {
        return if(combinationFailed)
        {"You have managed to properly recreate up to combination [$currentCombinationLength] out of [$maxCombinationLength]"}
        else
        {"You have completed all [$maxCombinationLength] combinations"}
    }

    override fun isGameOver(): Boolean {
        return currentCombinationLength>=maxCombinationLength||combinationFailed
    }

    override fun updateScore(result: Int) {
        if(result < 0) combinationFailed  = true
    }

    override fun getScore(): Int {
        return currentCombinationLength
    }
    override fun rollPointer(param: Int): Int {
        if(responseQueue.isEmpty()) initializeCombinationList(param)
        if(rollingPointer==responseQueue.count())
        {
            initializeCombinationList(param)
            rollingPointer=0
        }
        else if(rollingPointer==responseQueue.count()/2)
        {
            setup=false
        }
        return responseQueue[rollingPointer++]
    }
    private fun getRandomDistinctInt(excludedValue:Int?, maxIndex: Int): Int {
        var value: Int
        if(excludedValue==null) {
            return (-1 until maxIndex).random()
        }

        do {
            value = (-1 until maxIndex).random()
        } while (value == excludedValue)
        return value
    }

    private fun initializeCombinationList(maxIndex: Int) {
        currentCombinationLength++
        setup=true
        responseQueue.clear()
        responseQueue.add(getRandomDistinctInt(null,maxIndex))
        for (i in 1 until currentCombinationLength) {
            val valueToAdd = getRandomDistinctInt(responseQueue.last(), maxIndex)
            responseQueue.add(valueToAdd)

        }
        // Duplicate list
        responseQueue.addAll(responseQueue)
    }

}

data class FastReactionResults(
    var correctReactions :Int,
    var incorrectReactions :Int,
    var skips :Int,
    var correctReactionTime :Int,
    var incorrectReactionTime :Int,
)
