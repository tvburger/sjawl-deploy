package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;

import java.util.Collection;
import java.util.UUID;

public interface LocalServicesStore {

    interface Listener {

        <T> void serviceTypeAdded(LocalServicesStore state, Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy);

        <T> void serviceTypeRemoved(LocalServicesStore state, Class<T> serviceTypeClass);

        <T> void serviceRegistrationAdded(LocalServicesStore state, ServiceRegistration<T> serviceRegistration);

        <T> void serviceRegistrationRemoved(LocalServicesStore state, ServiceRegistration<T> serviceRegistration);

    }

    void addListener(Listener listener);

    void removeListener(Listener listener);

    Object getLock() throws DeployException;

    <T> void addServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException;

    <T> void removeServiceType(Class<T> serviceTypeClass) throws DeployException;

    <T> boolean hasServiceType(Class<T> serviceTypeClass) throws DeployException;

    Collection<Class<?>> getServiceTypes() throws DeployException;

    <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException;

    <T> void addServiceRegistration(ServiceRegistration<T> serviceRegistration) throws DeployException;

    void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException;

    boolean hasServiceRegistration(UUID serviceRegistrationId) throws DeployException;

    <T> ServiceRegistration<T> getServiceRegistration(UUID serviceRegistrationId) throws DeployException;

    <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException;

    default <T> void assertRegistered(Class<T> serviceTypeClass) throws DeployException {
        if (!hasServiceType(serviceTypeClass)) {
            throw new DeployException("Not registered serviceType: " + serviceTypeClass.getName());
        }
    }

}
