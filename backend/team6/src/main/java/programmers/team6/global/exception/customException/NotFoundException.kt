package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.NotFoundErrorCode

class NotFoundException(errorCode: NotFoundErrorCode) : CustomException(errorCode)
