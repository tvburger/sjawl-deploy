package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface RemoteServicesStore {

    interface Listener {

        void serviceTypeAdded(RemoteServicesStore store, String serviceTypeName, String deploymentStrategy);

        void serviceTypeRemoved(RemoteServicesStore store, String serviceTypeName);

        void serviceRegistrationAdded(RemoteServicesStore store, String serviceTypeName, RemoteServiceRegistration serviceRegistration);

        void serviceRegistrationRemoved(RemoteServicesStore store, String serviceTypeName, RemoteServiceRegistration serviceRegistration);

    }

    void addListener(Listener listener);

    void removeListener(Listener listener);

    Object getLock() throws DeployException, IOException;

    void addServiceType(String serviceTypeName, String deploymentStrategyName) throws DeployException, IOException;

    void removeServiceType(String serviceTypeName) throws DeployException, IOException;

    boolean hasServiceType(String serviceTypeName) throws DeployException, IOException;

    Collection<String> getServiceTypes() throws DeployException, IOException;

    String getServiceDeploymentStrategy(String serviceTypeName) throws DeployException, IOException;

    void addServiceRegistration(RemoteServiceRegistration serviceRegistration) throws DeployException, IOException;

    void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException;

    boolean hasServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException;

    RemoteServiceRegistration getServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException;

    Collection<RemoteServiceRegistration> getServiceRegistrations(String serviceTypeName) throws DeployException, IOException;

    default void assertRegistered(String serviceTypeName) throws DeployException, IOException {
        if (!hasServiceType(serviceTypeName)) {
            throw new DeployException("Not registered serviceType: " + serviceTypeName);
        }
    }

}
