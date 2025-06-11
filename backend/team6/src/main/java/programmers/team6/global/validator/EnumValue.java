package programmers.team6.global.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface EnumValue {
	Class<? extends Enum<?>> enumClass();

	String message() default "사용 할 수 없는 타입입니다";

	String fieldName();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
