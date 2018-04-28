package net.tvburger.sjawl.deploy.zookeeper.config;

import net.tvburger.sjawl.config.InvalidSettingException;
import net.tvburger.sjawl.config.InvalidSpecificationException;
import net.tvburger.sjawl.config.MissingSettingException;
import net.tvburger.sjawl.config.Specification;
import net.tvburger.sjawl.config.impl.AbstractConfigurationParser;
import net.tvburger.sjawl.config.util.ParserUtil;

import java.util.Arrays;
import java.util.List;

public class ZooKeeperConfigurationParser extends AbstractConfigurationParser<ZooKeeperConfiguration> {

    public static final List<String> FIELD_SESSION_TIMEOUT = Arrays.asList("zookeeper", "session_timeout");
    public static final List<String> FIELD_CONNECT_STRING = Arrays.asList("zookeeper", "connect_string");
    public static final List<String> FIELD_ONLY_JOIN_EXISTING = Arrays.asList("zookeeper", "only_join_existing");

    public static final int DEFAULT_SESSION_TIMEOUT = 2_000;
    public static final boolean DEFAULT_ONLY_JOIN_EXISTING = false;

    public ZooKeeperConfigurationParser() {
        super(ZooKeeperConfiguration.class);
    }

    @Override
    public ZooKeeperConfiguration parseConfiguration(Specification specification) throws InvalidSpecificationException {
        return new ZooKeeperConfiguration(
                parseConnectString(specification),
                parseSessionTimeout(specification),
                parseOnlyJoinExisting(specification));
    }

    private int parseSessionTimeout(Specification specification) throws MissingSettingException, InvalidSettingException {
        int sessionTimeout;
        if (specification.hasSetting(FIELD_SESSION_TIMEOUT)) {
            sessionTimeout = ParserUtil.parseInt(FIELD_SESSION_TIMEOUT, specification);
            if (sessionTimeout < 1) {
                throw new InvalidSettingException(specification, specification.getSetting(FIELD_SESSION_TIMEOUT));
            }
        } else {
            sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        }
        return sessionTimeout;
    }

    private String parseConnectString(Specification specification) throws MissingSettingException, InvalidSettingException {
        String connectString = specification.getValue(String.class, FIELD_CONNECT_STRING);
        if (connectString == null || connectString.isEmpty()) {
            throw new InvalidSettingException(specification, specification.getSetting(FIELD_CONNECT_STRING));
        }
        return connectString;
    }

    private boolean parseOnlyJoinExisting(Specification specification) throws MissingSettingException, InvalidSettingException {
        boolean existing;
        if (specification.hasSetting(FIELD_ONLY_JOIN_EXISTING)) {
            existing = ParserUtil.parseBoolean(FIELD_SESSION_TIMEOUT, specification);
        } else {
            existing = DEFAULT_ONLY_JOIN_EXISTING;
        }
        return existing;
    }

}
