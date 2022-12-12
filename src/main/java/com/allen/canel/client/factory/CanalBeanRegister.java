package com.allen.canel.client.factory;

import com.allen.canel.client.base.CanalBaseEntry;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/12
 */
public interface CanalBeanRegister {

    /**
     * 设置组件包
     * @param componentPackages
     */
    void setPackages(String ... componentPackages);

    /**
     * 获取组件包名
     * @return
     */
    String[] getPackages();

    /**
     * 注册 canal bean
     * @param entry
     */
    void register(CanalBaseEntry ... entry);
}
