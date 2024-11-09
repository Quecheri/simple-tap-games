package polsl.game.server.repository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

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
        return questionRepository.getNimQuestion()
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
    private var numberOfQuestions = 20
    override fun getQuestion(): Question
    {
        numberOfQuestions--
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
}

@Singleton
class QuestionRepository @Inject constructor(
) {
    fun getNimQuestion(): Question {
        val answers: List<Answer> = listOf(Answer("1",1),Answer("2",2),Answer("3",3))
        return Question("How many sticks?",answers,null)
    }
    fun getFastReactionQuestion(): Question {
        val answers: List<Answer> = listOf(Answer("Im not clicking",1),Answer("Im clicking",2))

        return if(Random.nextBoolean())
            Question("You should click",answers,2)
        else
            Question("You should not click",answers,1)
    }

}