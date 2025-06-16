package programmers.team6.global.exception.customException

import programmers.team6.global.exception.code.ErrorCode

abstract class CustomException protected constructor(@JvmField val errorCode: ErrorCode) :
    RuntimeException(errorCode.message)
