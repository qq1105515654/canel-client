package com.allen.canel.client.annotaion;

import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CanalTableColumn {

    @NotNull(message = "Field is empty")
    String columnName();
    //JDBC type
    JDBCType jdbcType() default JDBCType.NULL;
    //主键
    boolean isPrimary() default false;
}
