package com.allen.canel.client.factory;

import com.allen.canel.client.base.CanalBaseEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author snh
 * @description: TODO
 * @date 2022/4/12
 */
public class ComponentScanCanalBean extends AbstractCanalBeanFactory {


    public List<? extends CanalBaseEntry> loadingBeans() {
        String[] packages = this.getPackages();
        List<CanalBaseEntry> instances = new ArrayList<>();
        for (String basePackage : packages) {
            CanalBaseEntry[] packageBeans = getPackageBean(basePackage);
            if (packageBeans != null && packageBeans.length > 0) {
                List<CanalBaseEntry> collect = Arrays.stream(packageBeans).collect(Collectors.toList());
                instances.addAll(collect);
            }
        }
        return instances;
    }


    public <T extends CanalBaseEntry> T[] getPackageBean(String basePackage) {

        return null;
    }

    @Override
    public void register(CanalBaseEntry... entry) {
        super.register(entry);
    }


}
