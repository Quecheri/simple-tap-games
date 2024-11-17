package polsl.game.server.repository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class QuestionRepository @Inject constructor(
) {
    fun getNimQuestion(haystack : Int): Question {
        val answers: List<Answer> = listOf(Answer("1",1),Answer("2",2),Answer("3",3))
        return Question("How many sticks?",answers.take(haystack),null)
    }

    private var frQuestions = mutableListOf<Question>()
    private var frIdx = 0

    fun initFastReactionQuestions(numOfQuestions : Int)
    {
        val shouldClickRatio = 0.7f

        val shouldClick = (numOfQuestions*shouldClickRatio).toInt()
        val shouldNotClick = numOfQuestions - shouldClick

        frQuestions.clear()
        frIdx = 0

        repeat(shouldClick)
        {
            frQuestions.add( Question(SHOULD_CLICK, emptyList(),null))
        }

        repeat(shouldNotClick)
        {
            frQuestions.add( Question(SHOULD_NOT_CLICK,emptyList(),null))
        }

        frQuestions.shuffle()
    }

    fun getFastReactionQuestion(): Question {
        return frQuestions[frIdx++]
    }

}

internal const val SHOULD_CLICK = "s"
internal const val SHOULD_NOT_CLICK = "n"
