package com.allen.canel.client.factory;

import com.allen.canel.client.base.CanalBaseEntry;

import java.util.Map;

/**
 * canal 工具bean工厂，canal 映射实体的基类
 * @author allen
 */
public interface CanalBeanFactory {

    /**
     * 根据表名获取表实例
     * @param schema
     * @param tableName
     * @param <T>
     * @return
     */
    <T extends CanalBaseEntry> T getTableBean(String schema, String tableName);

    /**
     * 根据数据库名称以及表名称，以及列数据获取表实例。并填充数据
     * @param schema
     * @param tableName
     * @param columnValues
     * @param <T>
     * @return
     */
    <T extends CanalBaseEntry> T getTableBean(String schema, String tableName, Map<String,Object> columnValues);


}
