package cn.ac.iie.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME) 
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface FieldMeta {
	boolean isOptional() default false;
	String desc() default "";
}
