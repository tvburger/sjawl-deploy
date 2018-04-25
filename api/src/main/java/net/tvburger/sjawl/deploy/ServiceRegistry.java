package net.tvburger.sjawl.deploy;

import java.util.UUID;

public interface ServiceRegistry {

    default <T> UUID registerService(Class<T> serviceTypeClass, T serviceInstance) throws DeployException {
        return registerService(serviceTypeClass, serviceInstance, null);
    }

    <T> UUID registerService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException;

    void unregisterService(UUID serviceRegistrationId) throws DeployException;

    <T> void unregisterService(T serviceInstance) throws DeployException;

}
