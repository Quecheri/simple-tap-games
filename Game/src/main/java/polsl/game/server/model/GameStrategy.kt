package polsl.game.server.model

import android.util.Log
import polsl.game.server.repository.CONTROL_COMMUNICATION_FIRST
import polsl.game.server.repository.CONTROL_COMMUNICATION_SECOND
import polsl.game.server.repository.CONTROL_COMMUNICATION_THIRD
import polsl.game.server.repository.Prompt
import polsl.game.server.repository.PromptRepository
import polsl.game.server.repository.SHOULD_CLICK

//TODO ***usunięcie listy pytań z prompta

abstract class GameStrategy(protected val promptRepository: PromptRepository, protected val uintParam: UInt?)
{
    abstract fun getPrompt(): Prompt
    abstract fun getGameStateString(): String
    abstract fun isGameOver(): Boolean
    abstract fun updateScore(result: Int)
    abstract fun getScore() : Int

    protected var rollingPointer: Int = -1
    // Default implementation with sequential order.
    open fun rollPointer(param: Int) : Int
    {
        rollingPointer++
        if(rollingPointer==param) {
            rollingPointer = -1
        }
        return rollingPointer
    }
    protected fun getRandomDistinctPtr(currentRollingPointer:Int?, maxIndex: Int): Int {
        if(currentRollingPointer==null) {
            return (-1 until maxIndex).random()
        }
        val value: Int = (0 until maxIndex).random()
        if(value == currentRollingPointer)
            return -1
        return value
    }
}

class NimStrategy(promptRepository: PromptRepository,
                  uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{
    private var score: Int = uintParam?.toInt() ?: 20
    override fun getPrompt(): Prompt
    {
        return promptRepository.getNimPrompt(score)
    }

    override fun getGameStateString(): String {
        return "Wynik: $score"
    }

    override fun isGameOver(): Boolean {
        return score<=0
    }

    override fun updateScore(result: Int) {
        score -= result
    }

    override fun getScore(): Int {
        return score
    }
}

class FastReactionStrategy(promptRepository: PromptRepository,
                           uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{

    private var initialNumberOfPrompts :Int = uintParam?.toInt() ?: 20
    private var numberOfPrompts = initialNumberOfPrompts
    private var shouldClick = false
    private var results = FastReactionResults(0,0,0,0,0)

    init {
        promptRepository.initFastReactionPrompt(initialNumberOfPrompts);
    }
    override fun getPrompt(): Prompt
    {
        val q = promptRepository.getFastReactionPrompt()
        shouldClick = q.prompt == SHOULD_CLICK
        return q
    }

    override fun getGameStateString(): String {
        return "Liczba pozotałych rund: $numberOfPrompts"
    }

    override fun isGameOver(): Boolean {
        return numberOfPrompts <= 0
    }

    override fun updateScore(result: Int) {
        numberOfPrompts--

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
        return numberOfPrompts
    }

    fun getResultSting():String
    {
        val falseReactions = results.incorrectReactions + results.skips
        val nonFalseReactions = results.correctReactions
        val allReactions = falseReactions+nonFalseReactions

        val avgFalseTime = if(results.incorrectReactions>0) (results.incorrectReactionTime / results.incorrectReactions).toDouble() else 0.0
        val avgNonFalseTime = if(results.correctReactions>0) (results.correctReactionTime / results.correctReactions).toDouble() else 0.0

        val skipWord = when {
            results.skips == 1 -> "pominięcie"
            results.skips in 2..4 -> "pominięcia"
            else -> "pominięć"
        }
        val skipsStr = if(results.skips > 0) "(włączając ${results.skips} $skipWord) " else " "


        return """Niepoprawne reakcje $falseReactions ${skipsStr}z średnim czasem $avgFalseTime ms
Poprawne reakcje: $nonFalseReactions z średnim czasem $avgNonFalseTime ms
Wynik: $nonFalseReactions/$allReactions"""
    }

    override fun rollPointer(param: Int) : Int
    {
        rollingPointer = getRandomDistinctPtr(rollingPointer,param)
        return rollingPointer
    }
}
class CombinationStrategy(promptRepository: PromptRepository,
                  uintParam: UInt?
) : GameStrategy(promptRepository, uintParam)
{
    private var maxCombinationLength: Int = uintParam?.toInt() ?: 20
    private var currentCombinationLength: Int = 0
    private var combinationFailed = false
    private var setup = true
    private var controllMessageCounter = 2
    private var gameFinished = false
    private var responseQueue: MutableList<Int> = mutableListOf()
    private var combination: MutableList<Int> = mutableListOf()

    init {
        rollingPointer = 0
    }

    override fun getPrompt(): Prompt
    {
        val prompt = promptRepository.getCombinationPrompt(setup, controllMessageCounter)
        if(prompt.prompt== CONTROL_COMMUNICATION_FIRST ||
            prompt.prompt==CONTROL_COMMUNICATION_SECOND ||
            prompt.prompt==CONTROL_COMMUNICATION_THIRD )controllMessageCounter--
        return prompt
    }
    fun getSetup():Boolean
    {
        return setup
    }
    override fun getGameStateString(): String {
        return if(combinationFailed)
        {"Prawidłowo odtworzono [$currentCombinationLength] z [$maxCombinationLength] kombinacji"}
        else
        {"Prawidłowo odtworzono wszystkie [$maxCombinationLength] kombinacji"}
    }

    override fun isGameOver(): Boolean {
        return gameFinished||combinationFailed
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
        if(controllMessageCounter>0 && setup)return responseQueue[rollingPointer]
        else if(currentCombinationLength == maxCombinationLength && rollingPointer==responseQueue.count()-1)
        {
            gameFinished = true
        }
        else if(rollingPointer==responseQueue.count()/2 && setup)
        {
            setup=false
            return responseQueue[rollingPointer]
        }
        return responseQueue[rollingPointer++]
    }

    private fun initializeCombinationList(maxIndex: Int) {
        currentCombinationLength++
        setup=true
        controllMessageCounter = 2
        responseQueue.clear()

//      Every round randomize whole list
        combination.clear();
        combination.add(-1)
        for (i in 1 until currentCombinationLength) {
            combination.add(getRandomDistinctPtr(combination.lastOrNull() ?: -1, maxIndex))
        }

//        Every round add another element to existing list
//        if(currentCombinationLength==1)
//        {
//            combination.add(-1)
//        }
//        else
//        {
//            combination.add(getRandomDistinctPtr(combination.last(), maxIndex))
//        }

        responseQueue.addAll(combination)
        responseQueue.addAll(combination)
    }

}

data class FastReactionResults(
    var correctReactions :Int,
    var incorrectReactions :Int,
    var skips :Int,
    var correctReactionTime :Int,
    var incorrectReactionTime :Int,
)
