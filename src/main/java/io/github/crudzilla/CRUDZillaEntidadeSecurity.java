package io.github.crudzilla;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CRUDZillaEntidadeSecurity {
    CRUDZillaOperations operation();

    /**
     * name of the spring security Authority required
     */
    String value();
}
