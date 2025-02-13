package polsl.game.server.repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepository @Inject constructor(
) {
    private var frPrompt = mutableListOf<Prompt>()
    private var frIdx = 0
    fun getNimPrompt(haystack : Int): Prompt {
        val answers: List<Answer> = listOf(Answer("1",1),Answer("2",2),Answer("3",3))
        return Prompt("Ile zapałek weźmiesz?",answers.take(haystack),null)
    }
    fun initFastReactionPrompt(numOfPrompts : Int)
    {
        val shouldClickRatio = 0.7f

        val shouldClick = (numOfPrompts*shouldClickRatio).toInt()
        val shouldNotClick = numOfPrompts - shouldClick

        frPrompt.clear()
        frIdx = 0

        repeat(shouldClick)
        {
            frPrompt.add( Prompt(SHOULD_CLICK, emptyList(),null))
        }

        repeat(shouldNotClick)
        {
            frPrompt.add( Prompt(SHOULD_NOT_CLICK,emptyList(),null))
        }

        frPrompt.shuffle()
    }

    fun getFastReactionPrompt(): Prompt {
        return frPrompt[frIdx++]
    }
    fun getCombinationPrompt(setup: Boolean, controlMessageCounter:Int): Prompt {
        return when {
            controlMessageCounter == 2 -> Prompt(CONTROL_COMMUNICATION_FIRST, emptyList(), null)
            controlMessageCounter == 1 -> Prompt(CONTROL_COMMUNICATION_SECOND, emptyList(), null)
            controlMessageCounter == 0 && !setup-> Prompt(CONTROL_COMMUNICATION_THIRD, emptyList(), null)
            setup -> Prompt(SHOULD_NOT_CLICK, emptyList(), null)
            else -> Prompt(SHOULD_CLICK, emptyList(), null)
        }
    }

}

internal const val SHOULD_CLICK = "s"
internal const val CONTROL_COMMUNICATION_FIRST = "cf"
internal const val CONTROL_COMMUNICATION_SECOND = "cs"
internal const val CONTROL_COMMUNICATION_THIRD = "ct"
internal const val SHOULD_NOT_CLICK = "n"
