package polsl.game.server.repository

import polsl.game.proto.AnswerProto
import polsl.game.proto.QuestionProto


/**
 * Local Question Data.
 * @property questions list of questions with list of answers and correct answer id.
 */
data class Questions(
    val questions: List<Question>,
)

data class Question(
    val question: String,
    val answers: List<Answer>,
    val correctAnswerId: Int? = null,
)

data class Answer(
    val text: String,
    val id: Int,
)

fun Question.toProto() = QuestionProto(question, answers.map { it.toProto() })

fun Answer.toProto() = AnswerProto(text, id)

fun QuestionProto.toQuestion() = Question(text, answers.map { it.toAnswer() })

fun AnswerProto.toAnswer() = Answer(text, id)