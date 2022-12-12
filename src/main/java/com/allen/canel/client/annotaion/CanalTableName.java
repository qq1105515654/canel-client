package com.allen.canel.client.annotaion;

import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CanalTableName {

    @NotNull(message = "table name is empty")
    String value();

    @NotNull(message = "schema is empty")
    String schema();
}
