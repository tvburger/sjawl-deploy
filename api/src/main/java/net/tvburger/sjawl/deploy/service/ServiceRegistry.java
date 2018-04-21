package net.tvburger.sjawl.deploy.service;

import net.tvburger.sjawl.deploy.DeployException;

public interface ServiceRegistry {

    default <T> boolean isRegistered(Class<T> serviceTypeClass, T serviceInstance) throws DeployException {
        return isRegistered(serviceTypeClass, serviceInstance, null);
    }

    <T> boolean isRegistered(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException;

    default <T> void registerService(Class<T> serviceTypeClass, T serviceInstance) throws DeployException {
        registerService(serviceTypeClass, serviceInstance, null);
    }

    <T> void registerService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException;

    default <T> void unregisterService(Class<T> serviceTypeClass, T serviceInstance) throws DeployException {
        unregisterService(serviceTypeClass, serviceInstance, null);
    }

    <T> void unregisterService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException;

}
