package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServiceRegistrationRegistry {

    private final Map<UUID, ServiceRegistration<?>> serviceRegistrations = new HashMap<>();
    private final Map<ServiceRegistration<?>, UUID> serviceRegistrationIds = new HashMap<>();

    public void addServiceRegistration(ServiceRegistration<?> serviceRegistration, UUID serviceRegistrationId) throws DeployException {
        if (hasServiceRegistration(serviceRegistration) || hasServiceRegistration(serviceRegistrationId)) {
            throw new DeployException("Already registered!");
        }
        serviceRegistrations.put(serviceRegistrationId, serviceRegistration);
        serviceRegistrationIds.put(serviceRegistration, serviceRegistrationId);
    }

    public UUID addServiceRegistration(ServiceRegistration<?> serviceRegistration) throws DeployException {
        if (hasServiceRegistration(serviceRegistration)) {
            throw new DeployException("Already registered!");
        }
        UUID serviceRegistrationId = UUID.randomUUID();
        addServiceRegistration(serviceRegistration, serviceRegistrationId);
        return serviceRegistrationId;
    }

    public void removeServiceRegistration(ServiceRegistration<?> serviceRegistration) throws DeployException {
        if (!hasServiceRegistration(serviceRegistration)) {
            throw new DeployException("No such service registration!");
        }
        UUID serviceRegistrationId = serviceRegistrationIds.remove(serviceRegistration);
        serviceRegistrations.remove(serviceRegistrationId);
    }

    public boolean hasServiceRegistration(ServiceRegistration<?> serviceRegistration) {
        return serviceRegistrationIds.containsKey(serviceRegistration);
    }

    public boolean hasServiceRegistration(UUID serviceRegistrationId) {
        return serviceRegistrations.containsKey(serviceRegistrationId);
    }

    public UUID getServiceRegistrationId(ServiceRegistration<?> serviceRegistration) throws DeployException {
        if (!hasServiceRegistration(serviceRegistration)) {
            throw new DeployException("No such service registration present!");
        }
        return serviceRegistrationIds.get(serviceRegistration);
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceRegistration(UUID serviceId) throws DeployException {
        if (!hasServiceRegistration(serviceId)) {
            throw new DeployException("No such service registration present!");
        }
        return (T) serviceRegistrations.get(serviceId);
    }

}
