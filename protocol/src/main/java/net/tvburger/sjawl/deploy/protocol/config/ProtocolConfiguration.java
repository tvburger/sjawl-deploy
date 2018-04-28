package net.tvburger.sjawl.deploy.protocol.config;

import net.tvburger.sjawl.config.Configuration;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;

import java.util.UUID;

public class ProtocolConfiguration implements Configuration {

    private final UUID siteId;
    private final TcpAddress address;
    private final int serviceThreadCount;

    public ProtocolConfiguration(UUID siteId, TcpAddress address, int serviceThreadCount) {
        this.siteId = siteId;
        this.address = address;
        this.serviceThreadCount = serviceThreadCount;
    }

    public UUID getSiteId() {
        return siteId;
    }


    public TcpAddress getAddress() {
        return address;
    }

    public int getServiceThreadCount() {
        return serviceThreadCount;
    }

}
