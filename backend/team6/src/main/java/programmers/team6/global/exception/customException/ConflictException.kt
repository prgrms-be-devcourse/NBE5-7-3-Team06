package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.ConflictErrorCode

class ConflictException(errorCode: ConflictErrorCode) : CustomException(errorCode)
