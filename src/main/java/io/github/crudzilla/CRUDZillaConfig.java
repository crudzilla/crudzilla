package io.github.crudzilla;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CRUDZillaConfig {

    String key();

    boolean disableGetAll() default false;

    CRUDZillaEntidadeSecurity[] security() default {};
}
