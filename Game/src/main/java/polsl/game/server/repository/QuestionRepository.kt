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
        val answers: List<Answer> = listOf(Answer("Im not clicking",1),Answer("Im clicking",2))//TODO maybe answers can be removed

        return if(Random.nextBoolean())
            Question(SHOULD_CLICK,answers,2)
        else
            Question(SHOULD_NOT_CLICK,answers,1)
    }

}

internal const val SHOULD_CLICK = "s"
internal const val SHOULD_NOT_CLICK = "n"
