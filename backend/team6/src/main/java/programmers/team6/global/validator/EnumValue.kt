package programmers.team6.global.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValidator::class])
annotation class EnumValue(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "사용 할 수 없는 타입입니다",
    val fieldName: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
