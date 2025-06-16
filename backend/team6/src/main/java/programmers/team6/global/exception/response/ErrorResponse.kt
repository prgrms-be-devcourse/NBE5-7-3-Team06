package programmers.team6.global.exception.response


open class ErrorResponse(
    val codeName: String,
    val message: String,
    val status: Int
)