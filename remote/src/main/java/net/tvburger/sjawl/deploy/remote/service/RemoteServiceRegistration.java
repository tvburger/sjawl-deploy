package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.service.ServiceProperties;

import java.io.Serializable;
import java.util.UUID;

public final class RemoteServiceRegistration implements Serializable {

    private final UUID serviceRegistrationId;
    private final UUID siteId;
    private final ServiceProperties serviceProperties;

    public RemoteServiceRegistration(UUID serviceRegistrationId, UUID siteId, ServiceProperties serviceProperties) {
        this.serviceRegistrationId = serviceRegistrationId;
        this.siteId = siteId;
        this.serviceProperties = serviceProperties;
    }

    public UUID getServiceRegistrationId() {
        return serviceRegistrationId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

}
