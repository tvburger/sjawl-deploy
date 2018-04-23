package net.tvburger.sjawl.deploy.local.service;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.util.Collection;

public interface LocalServicesStateManager {

    interface Listener {

        <T> void serviceTypeAdded(LocalServicesStateManager state, Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy);

        <T> void serviceTypeRemoved(LocalServicesStateManager state, Class<T> serviceTypeClass);

        <T> void serviceRegistrationAdded(LocalServicesStateManager state, Class<T> serviceTypeClass, ServiceRegistration serviceRegistration);

        <T> void serviceRegistrationRemoved(LocalServicesStateManager state, Class<T> serviceTypeClass, ServiceRegistration serviceRegistration);

    }

    void addListener(Listener listener);

    void removeListener(Listener listener);

    Object getLock() throws DeployException;

    <T> void addServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException;

    <T> void removeServiceType(Class<T> serviceTypeClass) throws DeployException;

    <T> boolean hasServiceType(Class<T> serviceTypeClass) throws DeployException;

    Collection<Class<?>> getServiceTypeClasses() throws DeployException;

    <T> ServiceDeploymentStrategy<T> getServiceTypeDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException;

    <T> void addServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) throws DeployException;

    <T> void removeServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) throws DeployException;

    <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException;

    default <T> void assertRegistered(Class<T> serviceTypeClass) throws DeployException {
        if (!hasServiceType(serviceTypeClass)) {
            throw new DeployException("Not registered serviceType: " + serviceTypeClass.getName());
        }
    }

}
