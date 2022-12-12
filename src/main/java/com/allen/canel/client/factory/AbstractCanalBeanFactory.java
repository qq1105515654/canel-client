package com.allen.canel.client.factory;

import com.allen.canel.client.annotaion.CanalTableColumn;
import com.allen.canel.client.base.CanalBaseEntry;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/12
 */
public class AbstractCanalBeanFactory implements CanalBeanFactory, CanalBeanRegister {

    private String[] basePackages;

    /**
     * 对应数据库中每个表的注册实例
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ? extends CanalBaseEntry>> schemaRegisterMap = new ConcurrentHashMap<>(16);

    @Override
    public <T extends CanalBaseEntry> T getTableBean(String schema, String tableName) {
        ConcurrentHashMap<String, ? extends CanalBaseEntry> tableMap = schemaRegisterMap.get(schema);
        if (tableMap != null && !tableMap.isEmpty()) {
            return (T) tableMap.get(tableName);
        }
        return null;
    }

    @Override
    public <T extends CanalBaseEntry> T getTableBean(String schema, String tableName, Map<String, Object> columnValues) {
        ConcurrentHashMap<String, ? extends CanalBaseEntry> tableMap = schemaRegisterMap.get(schema);
        if(tableMap!=null && !tableMap.isEmpty()){
            return (T) loadingColumn(tableMap.get(tableName),columnValues);
        }
        return null;
    }

    @Override
    public void setPackages(String... componentPackages) {
        this.basePackages = componentPackages;
    }

    @Override
    public String[] getPackages() {
        return this.basePackages;
    }

    @Override
    public void register(CanalBaseEntry... entry) {
        throw new CanalRegisterException("Canal Bean entry register error!");
    }


    public <T extends CanalBaseEntry> T loadingColumn(T instance, Map<String, Object> columnValues) {
        if(instance == null){
            throw new CanalLoadingBeanColumnException("Canal loading bean instance is empty.");
        }
        Class<? extends CanalBaseEntry> clazz = instance.getClass();
        if (columnValues == null || columnValues.isEmpty()) {
            throw new CanalLoadingBeanColumnException("Canal loading bean entry column values exception.");
        }
        try {
            Set<String> keySet = columnValues.keySet();
            for (String column : keySet) {
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    CanalTableColumn canalColumn = getFieldAnnotation(field.getAnnotations());
                    if (canalColumn == null) {
                        continue;
                    }
                    String columnName = canalColumn.columnName();
                    if (StringUtils.equals(column, columnName)) {
                        field.setAccessible(true);
                        field.set(instance, columnValues.get(column));
                        break;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }


    private CanalTableColumn getFieldAnnotation(Annotation[] fieldAnnotations) {
        for (Annotation fieldAnnotation : fieldAnnotations) {
            if (fieldAnnotation instanceof CanalTableColumn) {
                return (CanalTableColumn) fieldAnnotation;
            }
        }
        return null;
    }
}
