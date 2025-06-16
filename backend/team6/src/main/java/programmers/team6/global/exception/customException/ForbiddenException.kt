package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.ForbiddenErrorCode

class ForbiddenException(errorCode: ForbiddenErrorCode) : CustomException(errorCode)
