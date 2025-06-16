package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.UnauthorizedErrorCode

class UnauthorizedException(errorCode: UnauthorizedErrorCode) : CustomException(errorCode)
