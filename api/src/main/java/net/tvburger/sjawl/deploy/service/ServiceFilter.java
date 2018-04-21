package net.tvburger.sjawl.deploy.service;

import net.tvburger.sjawl.config.MissingSettingException;
import net.tvburger.sjawl.config.Specification;

public interface ServiceFilter extends Specification {

    default boolean matches(ServiceProperties serviceProperties) {
        if (serviceProperties == null) {
            return false;
        }
        try {
            for (Specification.Setting setting : this) {
                if (!serviceProperties.hasSetting(setting.getField())) {
                    return false;
                }
                Specification.Setting property = serviceProperties.getSetting(setting.getField());
                if (!property.getValue().equals(setting.getValue())) {
                    return false;
                }
            }
            return true;
        } catch (MissingSettingException cause) {
            return false;
        }
    }

}
