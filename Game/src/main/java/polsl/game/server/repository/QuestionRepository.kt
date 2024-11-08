package polsl.game.server.repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
) {




    fun getQuestion(): Question {
        val answers: List<Answer> = listOf(Answer("1",1),Answer("2",2),Answer("3",3))
        return Question("How many sticks?",answers,null)
    }

}