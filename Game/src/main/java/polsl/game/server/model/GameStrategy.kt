package polsl.game.server.model

import polsl.game.server.repository.Prompt
import polsl.game.server.repository.PromptRepository
import polsl.game.server.repository.SHOULD_CLICK

//TODO ekran z instrukcją i licencją
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

        val skipWord = if(results.skips == 1) "skip" else "skips"
        val skipsStr = if(results.skips > 0) "(including ${results.skips} $skipWord) " else " "


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
    private var gameFinished = false
    private var responseQueue: MutableList<Int> = mutableListOf()
    private var combination: MutableList<Int> = mutableListOf()

    init {
        rollingPointer = 0
    }

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
        else if(currentCombinationLength == maxCombinationLength && rollingPointer==responseQueue.count()-1)
        {
            gameFinished = true
        }
        else if(rollingPointer==responseQueue.count()/2)
        {
            setup=false
        }
        return responseQueue[rollingPointer++]
    }

    private fun initializeCombinationList(maxIndex: Int) {
        currentCombinationLength++
        setup=true
        responseQueue.clear()
        if(currentCombinationLength==1)
        {
            combination.add(-1)
        }
        else
        {
            combination.add(getRandomDistinctPtr(combination.last(), maxIndex))
        }

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
