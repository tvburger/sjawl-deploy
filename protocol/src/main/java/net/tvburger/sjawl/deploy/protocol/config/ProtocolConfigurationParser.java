package net.tvburger.sjawl.deploy.protocol.config;

import net.tvburger.sjawl.config.InvalidSettingException;
import net.tvburger.sjawl.config.InvalidSpecificationException;
import net.tvburger.sjawl.config.MissingSettingException;
import net.tvburger.sjawl.config.Specification;
import net.tvburger.sjawl.config.impl.AbstractConfigurationParser;
import net.tvburger.sjawl.config.util.ParserUtil;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProtocolConfigurationParser extends AbstractConfigurationParser<ProtocolConfiguration> {

    public static final List<String> FIELD_SITE_ID = Arrays.asList("site", "id");
    public static final List<String> FIELD_BIND_PORT = Arrays.asList("site", "listen_port");
    public static final List<String> FIELD_BIND_ADDRESS = Arrays.asList("site", "listen_address");
    public static final List<String> FIELD_SERVICE_THREAD_COUNT = Arrays.asList("site", "nr_of_threads");

    public static final int DEFAULT_BIND_PORT = 0;
    public static final int DEFAULT_SERVICE_THREAD_COUNT = 4;

    public ProtocolConfigurationParser() {
        super(ProtocolConfiguration.class);
    }

    @Override
    public ProtocolConfiguration parseConfiguration(Specification specification) throws InvalidSpecificationException {
        return new ProtocolConfiguration(
                parseSiteId(specification),
                parseTcpAddress(specification),
                parseServiceThreadCount(specification));
    }

    private UUID parseSiteId(Specification specification) throws MissingSettingException, InvalidSettingException {
        try {
            UUID siteId;
            if (specification.hasSetting(FIELD_SITE_ID)) {
                siteId = UUID.fromString(specification.getValue(String.class, FIELD_SITE_ID));
            } else {
                siteId = null;
            }
            return siteId;
        } catch (IllegalArgumentException cause) {
            throw new InvalidSettingException(specification, specification.getSetting(FIELD_SITE_ID));
        }
    }

    private TcpAddress parseTcpAddress(Specification specification) throws MissingSettingException, InvalidSettingException {
        return new TcpAddress(parseBindAddress(specification), parseBindPort(specification));
    }

    private InetAddress parseBindAddress(Specification specification) throws MissingSettingException, InvalidSettingException {
        try {
            InetAddress bindAddress;
            if (specification.hasSetting(FIELD_BIND_ADDRESS)) {
                String bindAddressString = specification.getValue(String.class, FIELD_BIND_ADDRESS);
                if (bindAddressString == null || bindAddressString.isEmpty()) {
                    throw new InvalidSettingException(specification, specification.getSetting(FIELD_BIND_ADDRESS));
                }
                bindAddress = InetAddress.getByName(bindAddressString);
            } else {
                bindAddress = InetAddress.getLocalHost();
            }
            return bindAddress;
        } catch (UnknownHostException cause) {
            throw new InvalidSettingException(specification, specification.getSetting(FIELD_BIND_ADDRESS));
        }
    }

    private int parseBindPort(Specification specification) throws MissingSettingException, InvalidSettingException {
        int bindPort;
        if (specification.hasSetting(FIELD_BIND_PORT)) {
            bindPort = ParserUtil.parseInt(FIELD_BIND_PORT, specification);
            if (bindPort < 1) {
                throw new InvalidSettingException(specification, specification.getSetting(FIELD_BIND_PORT));
            }
        } else {
            bindPort = DEFAULT_BIND_PORT;
        }
        return bindPort;
    }

    private int parseServiceThreadCount(Specification specification) throws MissingSettingException, InvalidSettingException {
        int serviceThreadCount;
        if (specification.hasSetting(FIELD_SERVICE_THREAD_COUNT)) {
            serviceThreadCount = ParserUtil.parseInt(FIELD_SERVICE_THREAD_COUNT, specification);
            if (serviceThreadCount < 0) {
                throw new InvalidSettingException(specification, specification.getSetting(FIELD_SERVICE_THREAD_COUNT));
            }
        } else {
            serviceThreadCount = DEFAULT_SERVICE_THREAD_COUNT;
        }
        return serviceThreadCount;
    }
}
