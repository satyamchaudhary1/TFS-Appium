package com.tfs.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestInfo {
    String id();
    String title();
    String module();
    boolean requiresDriver() default true;
}
