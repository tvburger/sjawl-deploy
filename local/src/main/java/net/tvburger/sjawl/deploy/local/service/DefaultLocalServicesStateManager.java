package net.tvburger.sjawl.deploy.local.service;

import net.tvburger.sjawl.deploy.service.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.util.*;

public final class DefaultLocalServicesStateManager implements LocalServicesStateManager {

    private final Set<Listener> listeners = new HashSet<>();
    private final Object lock = new Object();
    private final Map<Class<?>, ServiceDeploymentStrategy<?>> serviceTypes = new HashMap<>();
    private final Map<Class<?>, List<ServiceRegistration<?>>> serviceRegistrations = new HashMap<>();

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
    public Collection<Class<?>> getServiceTypeClasses() {
        return Collections.unmodifiableCollection(serviceTypes.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceTypeDeploymentStrategy(Class<T> serviceTypeClass) {
        return (ServiceDeploymentStrategy) serviceTypes.get(serviceTypeClass);
    }

    @Override
    public <T> void addServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) {
        serviceRegistrations.get(serviceTypeClass).add(serviceRegistration);
        for (Listener listener : listeners) {
            listener.serviceRegistrationAdded(this, serviceTypeClass, serviceRegistration);
        }
    }

    @Override
    public <T> void removeServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) {
        serviceRegistrations.get(serviceTypeClass).remove(serviceRegistration);
        for (Listener listener : listeners) {
            listener.serviceRegistrationRemoved(this, serviceTypeClass, serviceRegistration);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) {
        return (Collection) Collections.unmodifiableCollection(serviceRegistrations.get(serviceTypeClass));
    }

}
