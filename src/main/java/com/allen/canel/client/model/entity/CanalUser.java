package com.allen.canel.client.model.entity;

import com.allen.canel.client.annotaion.CanalTableColumn;
import com.allen.canel.client.annotaion.CanalTableName;
import com.allen.canel.client.base.CanalBaseEntry;
import lombok.Data;

import java.io.Serializable;
import java.sql.JDBCType;
import java.time.LocalDateTime;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/12
 */
@Data
@CanalTableName(value = "canal_user", schema = "canal")
public class CanalUser extends CanalBaseEntry implements Serializable {

    @CanalTableColumn(columnName = "id", jdbcType = JDBCType.BIGINT, isPrimary = true)
    private Long id;

    @CanalTableColumn(columnName = "username", jdbcType = JDBCType.VARCHAR)
    private String username;

    @CanalTableColumn(columnName = "password", jdbcType = JDBCType.VARCHAR)
    private String password;

    @CanalTableColumn(columnName = "mobile", jdbcType = JDBCType.VARCHAR)
    private String mobile;

    @CanalTableColumn(columnName = "wx_openid", jdbcType = JDBCType.VARCHAR)
    private String wxOpenId;

    @CanalTableColumn(columnName = "create_time", jdbcType = JDBCType.TIMESTAMP)
    private LocalDateTime createTime;

    @CanalTableColumn(columnName = "create_user", jdbcType = JDBCType.VARCHAR)
    private String createUser;

    @CanalTableColumn(columnName = "modify_time", jdbcType = JDBCType.TIMESTAMP)
    private LocalDateTime modifyTime;

    @CanalTableColumn(columnName = "modify_user", jdbcType = JDBCType.VARCHAR)
    private String modifyUser;

    @CanalTableColumn(columnName = "version", jdbcType = JDBCType.INTEGER)
    private Integer version;

    @CanalTableColumn(columnName = "enabled", jdbcType = JDBCType.TINYINT)
    private Boolean enabled;

    @CanalTableColumn(columnName = "deleteFlag", jdbcType = JDBCType.TINYINT)
    private Boolean deleteFlag;


}
