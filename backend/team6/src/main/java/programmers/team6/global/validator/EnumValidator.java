package programmers.team6.global.validator;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.springframework.util.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumValue, String> {

	private EnumValue enumValue;

	@Override
	public void initialize(final EnumValue constraintAnnotation) {
		this.enumValue = constraintAnnotation;
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (ObjectUtils.isEmpty(value)) {
			return true;
		}
		final Enum<?>[] enumConstants = this.enumValue.enumClass().getEnumConstants();
		if (enumConstants == null) {
			context.buildConstraintViolationWithTemplate(enumValue.message())
				.addConstraintViolation();
			return false;
		}
		boolean result = Arrays.stream(enumConstants)
			.anyMatch(enumConstant -> convertible(value, enumConstant));
		if (!result) {
			context.buildConstraintViolationWithTemplate(enumValue.message())
				.addConstraintViolation();
			return result;
		}
		return result;
	}

	private boolean convertible(String value, final Enum<?> enumConstant) {
		try {
			// Enum 클래스의 모든 필드를 가져옴
			Field[] fields = enumConstant.getClass().getDeclaredFields();

			// 필드들을 순회하며 'code' 필드가 존재하는지 확인
			for (Field field : fields) {
				if (!field.getName().equals(enumValue.fieldName())) {
					continue;
				}

				field.setAccessible(true);
				String codeValue = (String)field.get(enumConstant);
				if (value.trim().equalsIgnoreCase(codeValue)) {
					return true;
				}
			}
			return false;
		} catch (IllegalAccessException e) {
			return false;
		}
	}
}
