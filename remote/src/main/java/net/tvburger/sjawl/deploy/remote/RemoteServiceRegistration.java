package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.ServiceProperties;

import java.io.Serializable;
import java.util.UUID;

public final class RemoteServiceRegistration implements Serializable {

    private final UUID registrationId;
    private final String serviceTypeName;
    private final UUID serviceId;
    private final UUID siteId;
    private final ServiceProperties serviceProperties;

    public RemoteServiceRegistration(UUID registrationId, String serviceTypeName, UUID serviceId, UUID siteId, ServiceProperties serviceProperties) {
        this.registrationId = registrationId;
        this.serviceTypeName = serviceTypeName;
        this.serviceId = serviceId;
        this.siteId = siteId;
        this.serviceProperties = serviceProperties;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public String getServiceType() {
        return serviceTypeName;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

}
