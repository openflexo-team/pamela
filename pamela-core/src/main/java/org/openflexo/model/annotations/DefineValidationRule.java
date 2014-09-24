package org.openflexo.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to associate a validation rule to a PAMELA entity
 * 
 * @author sylvain
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface DefineValidationRule {

	public static enum ValidationRulePolicy {
		ENABLED, DISABLED;
	}

	ValidationRulePolicy value() default ValidationRulePolicy.ENABLED;

}
