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
        val answers: List<Answer> = listOf(Answer("Im not clicking",1),Answer("Im clicking",2))

        return if(Random.nextBoolean())
            Question("You should click",answers,2)
        else
            Question("You should not click",answers,1)
    }

}