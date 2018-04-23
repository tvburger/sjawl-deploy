package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.DeployException;

import java.io.IOException;
import java.util.Collection;

public interface RemoteServicesStateManager {

    interface Listener {

        void serviceTypeAdded(RemoteServicesStateManager stateManager, String serviceTypeName, String deploymentStrategy);

        void serviceTypeRemoved(RemoteServicesStateManager stateManager, String serviceTypeName);

        void serviceRegistrationAdded(RemoteServicesStateManager stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration);

        void serviceRegistrationRemoved(RemoteServicesStateManager stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration);

    }

    void addListener(Listener listener);

    void removeListener(Listener listener);

    Object getLock() throws DeployException, IOException;

    void addServiceType(String serviceTypeName, String deploymentStrategyName) throws DeployException, IOException;

    void removeServiceType(String serviceTypeName) throws DeployException, IOException;

    boolean hasServiceType(String serviceTypeName) throws DeployException, IOException;

    Collection<String> getServiceTypeNames() throws DeployException, IOException;

    String getServiceTypeDeploymentName(String serviceTypeName) throws DeployException, IOException;

    void addServiceRegistration(String serviceTypeName, RemoteServiceRegistration serviceRegistration) throws DeployException, IOException;

    void removeServiceRegistration(String serviceTypeName, RemoteServiceRegistration serviceRegistration) throws DeployException, IOException;

    Collection<RemoteServiceRegistration> getServiceRegistrations(String serviceTypeName) throws DeployException, IOException;

    default void assertRegistered(String serviceTypeName) throws DeployException, IOException {
        if (!hasServiceType(serviceTypeName)) {
            throw new DeployException("Not registered serviceType: " + serviceTypeName);
        }
    }

}
