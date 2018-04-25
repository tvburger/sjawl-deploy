package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.config.MissingSettingException;
import net.tvburger.sjawl.config.impl.DecoratedSpecification;
import net.tvburger.sjawl.config.impl.MapSpecification;
import net.tvburger.sjawl.deploy.ServiceFilter;
import net.tvburger.sjawl.deploy.ServiceProperties;

import java.util.Collections;

public class LanguageSpecification extends DecoratedSpecification implements ServiceFilter, ServiceProperties {

    public static final class Factory {

        public static LanguageSpecification create(String language) {
            return new LanguageSpecification(MapSpecification.Factory.create(
                    Collections.singletonMap(Collections.singletonList("language"), language)));
        }

        private Factory() {
        }

    }

    private LanguageSpecification(MapSpecification specification) {
        super(specification);
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
