package polsl.game.server.repository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PromptRepository @Inject constructor(
) {
    fun getNimPrompt(haystack : Int): Prompt {
        val answers: List<Answer> = listOf(Answer("1",1),Answer("2",2),Answer("3",3))
        return Prompt("How many sticks?",answers.take(haystack),null)
    }
    fun getFastReactionPrompt(): Prompt {
        val randInt = Random.nextInt(1000)
        return if(randInt<800)
            Prompt(SHOULD_CLICK, emptyList(),null)
        else
            Prompt(SHOULD_NOT_CLICK,emptyList(),null)
    }
    fun getCombinationPrompt(setup:Boolean): Prompt {
        return if(setup) Prompt(SHOULD_NOT_CLICK, emptyList(),null)
        else Prompt(SHOULD_CLICK, emptyList(),null)
    }

}

internal const val SHOULD_CLICK = "s"
internal const val SHOULD_NOT_CLICK = "n"
