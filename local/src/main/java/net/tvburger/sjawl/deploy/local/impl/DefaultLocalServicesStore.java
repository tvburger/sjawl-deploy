package net.tvburger.sjawl.deploy.local.impl;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.utils.ServiceRegistrationRegistry;

import java.util.*;

public final class DefaultLocalServicesStore implements LocalServicesStore {

    private final Set<Listener> listeners = new HashSet<>();
    private final Object lock = new Object();
    private final Map<Class<?>, ServiceDeploymentStrategy<?>> serviceTypes = new HashMap<>();
    private final Map<Class<?>, List<ServiceRegistration<?>>> serviceRegistrations = new HashMap<>();
    private final ServiceRegistrationRegistry registry = new ServiceRegistrationRegistry();

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public Object getLock() {
        return lock;
    }

    @Override
    public <T> void addServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) {
        serviceTypes.put(serviceTypeClass, deploymentStrategy);
        serviceRegistrations.put(serviceTypeClass, new ArrayList<>());
        for (Listener listener : listeners) {
            listener.serviceTypeAdded(this, serviceTypeClass, deploymentStrategy);
        }
    }

    @Override
    public <T> void removeServiceType(Class<T> serviceTypeClass) {
        serviceTypes.remove(serviceTypeClass);
        serviceRegistrations.remove(serviceTypeClass);
        for (Listener listener : listeners) {
            listener.serviceTypeRemoved(this, serviceTypeClass);
        }
    }

    @Override
    public <T> boolean hasServiceType(Class<T> serviceTypeClass) {
        return serviceTypes.containsKey(serviceTypeClass);
    }

    @Override
    public Collection<Class<?>> getServiceTypes() {
        return Collections.unmodifiableCollection(serviceTypes.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) {
        return (ServiceDeploymentStrategy) serviceTypes.get(serviceTypeClass);
    }

    @Override
    public <T> void addServiceRegistration(ServiceRegistration<T> serviceRegistration) throws DeployException {
        serviceRegistrations.get(serviceRegistration.getServiceType()).add(serviceRegistration);
        registry.addServiceRegistration(serviceRegistration);
        for (Listener listener : listeners) {
            listener.serviceRegistrationAdded(this, serviceRegistration);
        }
    }

    @Override
    public void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        ServiceRegistration<?> serviceRegistration = registry.getServiceRegistration(serviceRegistrationId);
        serviceRegistrations.get(serviceRegistration.getServiceType()).remove(serviceRegistration);
        registry.removeServiceRegistration(serviceRegistrationId);
        for (Listener listener : listeners) {
            listener.serviceRegistrationRemoved(this, serviceRegistration);
        }
    }

    @Override
    public boolean hasServiceRegistration(UUID serviceRegistrationId) {
        return registry.hasServiceRegistration(serviceRegistrationId);
    }

    @Override
    public <T> ServiceRegistration<T> getServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        return registry.getServiceRegistration(serviceRegistrationId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) {
        return (Collection) Collections.unmodifiableCollection(serviceRegistrations.get(serviceTypeClass));
    }

}
