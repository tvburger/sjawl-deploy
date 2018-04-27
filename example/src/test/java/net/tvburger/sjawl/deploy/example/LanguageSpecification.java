package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.config.MissingSettingException;
import net.tvburger.sjawl.config.impl.MapSpecification;
import net.tvburger.sjawl.deploy.ServiceFilter;
import net.tvburger.sjawl.deploy.ServiceProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LanguageSpecification extends MapSpecification implements ServiceFilter, ServiceProperties {

    public static final class Factory {

        public static LanguageSpecification create(String language) {
            return new LanguageSpecification(Collections.singletonMap(Collections.singletonList("language"), language));
        }

        private Factory() {
        }

    }

    private LanguageSpecification() {
        super(null);
    }

    private LanguageSpecification(Map<List<String>, Object> settingsMap) {
        super(settingsMap);
    }

    @Override
    public String toString() {
        try {
            return "LanguageSpecification: " + getValue(Collections.singletonList("language"));
        } catch (MissingSettingException cause) {
            return "LanguageSpecification: -";
        }
    }

}
