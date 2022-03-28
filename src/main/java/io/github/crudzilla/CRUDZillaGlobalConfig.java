package io.github.crudzilla;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CRUDZillaGlobalConfig {

    /**
     * Wich packages should be scanned for all the reflections operations
     */
    String scanPackage();

    String domainPackagePrefix() default ".domain";

    String appPackagePrefix() default ".app";

}
