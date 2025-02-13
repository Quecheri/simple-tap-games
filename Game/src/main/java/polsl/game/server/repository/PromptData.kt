package polsl.game.server.repository

import polsl.game.proto.AnswerProto
import polsl.game.proto.PromptProto


/**
 * Local Question Data.
 * @property prompts list of questions with list of answers and correct answer id.
 */
data class Prompts(
    val prompts: List<Prompt>,
)

data class Prompt(
    val prompt: String,
    val answers: List<Answer>,
    val correctAnswerId: Int? = null,
)

data class Answer(
    val text: String,
    val id: Int,
)

fun Prompt.toProto() = PromptProto(prompt, answers.map { it.toProto() })

fun Answer.toProto() = AnswerProto(text, id)

fun PromptProto.toPrompt() = Prompt(text, answers.map { it.toAnswer() },correctAnswerId)

fun AnswerProto.toAnswer() = Answer(text, id)