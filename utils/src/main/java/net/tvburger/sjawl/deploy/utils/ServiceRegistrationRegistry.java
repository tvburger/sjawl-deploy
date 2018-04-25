package net.tvburger.sjawl.deploy.utils;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;

import java.util.*;

public class ServiceRegistrationRegistry {

    private final Map<UUID, ServiceRegistration<?>> serviceRegistrations = new HashMap<>();

    public <T> void addServiceRegistration(ServiceRegistration<T> serviceRegistration) throws DeployException {
        AssertUtil.assertNotNull(serviceRegistration);
        if (hasServiceRegistration(serviceRegistration)) {
            throw new DeployException("Already registered!");
        }
        serviceRegistrations.put(serviceRegistration.getRegistrationId(), serviceRegistration);
    }

    public void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        AssertUtil.assertNotNull(serviceRegistrationId);
        if (!hasServiceRegistration(serviceRegistrationId)) {
            throw new DeployException("No such service registration!");
        }
        serviceRegistrations.remove(serviceRegistrationId);
    }

    public boolean hasServiceRegistration(ServiceRegistration<?> serviceRegistration) {
        AssertUtil.assertNotNull(serviceRegistration);
        return hasServiceRegistration(serviceRegistration.getRegistrationId());
    }

    public boolean hasServiceRegistration(UUID serviceRegistrationId) {
        AssertUtil.assertNotNull(serviceRegistrationId);
        return serviceRegistrations.containsKey(serviceRegistrationId);
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        AssertUtil.assertNotNull(serviceRegistrationId);
        if (!hasServiceRegistration(serviceRegistrationId)) {
            throw new DeployException("No such service registration present!");
        }
        return (T) serviceRegistrations.get(serviceRegistrationId);
    }

    public Collection<UUID> getServiceRegistrationIds() {
        return new HashSet<>(serviceRegistrations.keySet());
    }

    public <T> Collection<UUID> getServiceRegistrationIds(T serviceInstance) throws DeployException {
        AssertUtil.assertNotNull(serviceInstance);
        Set<UUID> serviceRegistrationIds = new HashSet<>();
        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations.values()) {
            if (Objects.equals(serviceRegistration.getServiceInstance(), serviceInstance)) {
                serviceRegistrationIds.add(serviceRegistration.getRegistrationId());
            }
        }
        return serviceRegistrationIds;
    }

}
