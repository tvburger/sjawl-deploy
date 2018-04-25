package net.tvburger.sjawl.deploy.admin;

import net.tvburger.sjawl.deploy.ServiceProperties;

import java.util.Objects;
import java.util.UUID;

public final class ServiceRegistration<T> {

    private final UUID registrationId;
    private final Class<T> serviceTypeClass;
    private final T serviceInstance;
    private final ServiceProperties serviceProperties;

    public ServiceRegistration(UUID registrationId, Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) {
        this.registrationId = registrationId;
        this.serviceTypeClass = serviceTypeClass;
        this.serviceInstance = serviceInstance;
        this.serviceProperties = serviceProperties;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public Class<T> getServiceType() {
        return serviceTypeClass;
    }

    public T getServiceInstance() {
        return serviceInstance;
    }

    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ServiceRegistration)) {
            return false;
        }
        ServiceRegistration<?> other = (ServiceRegistration<?>) object;
        return Objects.equals(registrationId, other.registrationId);
    }

    @Override
    public int hashCode() {
        return registrationId.hashCode();
    }

}
