package programmers.team6.global.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<EnumValue?, String> {

    private var enumValue: EnumValue? = null

    override fun initialize(constraintAnnotation: EnumValue?) {
        this.enumValue = constraintAnnotation
    }

    override fun isValid(value: String, context: ConstraintValidatorContext): Boolean {
        val enumValue = this.enumValue ?: return true

        if (value.isBlank()) {
            return true
        }

        val enumConstants = enumValue.enumClass.java.enumConstants ?: return false

        val result = enumConstants.any { enumConstant ->
            convertible(value, enumConstant, enumValue.fieldName)
        }

        if (!result) {
            context.buildConstraintViolationWithTemplate(enumValue.message)
                .addConstraintViolation()
                .disableDefaultConstraintViolation()
        }

        return result
    }

    private fun convertible(value: String, enumConstant: Enum<*>, fieldName: String): Boolean {
        return try {
            val field = enumConstant.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val codeValue = field[enumConstant] as? String ?: return false
            value.trim().equals(codeValue, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
