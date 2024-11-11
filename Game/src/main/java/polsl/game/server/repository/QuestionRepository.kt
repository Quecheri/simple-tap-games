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
    fun getFastReactionQuestion(): Question {
        val randInt = Random.nextInt(1000)
        return if(randInt<800)
            Question(SHOULD_CLICK, emptyList(),null)
        else
            Question(SHOULD_NOT_CLICK,emptyList(),null)
    }

}

internal const val SHOULD_CLICK = "s"
internal const val SHOULD_NOT_CLICK = "n"
