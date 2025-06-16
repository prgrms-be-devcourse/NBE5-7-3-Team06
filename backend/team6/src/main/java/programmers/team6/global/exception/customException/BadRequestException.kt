package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.BadRequestErrorCode

class BadRequestException(errorCode: BadRequestErrorCode) : CustomException(errorCode)
