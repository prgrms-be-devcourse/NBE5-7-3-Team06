package programmers.team6.global.exception.response



class ValidationErrorResponse(
    codeName: String,
    message: String,
    status: Int,
    val errors: Map<String, String>
) : ErrorResponse(codeName, message, status)