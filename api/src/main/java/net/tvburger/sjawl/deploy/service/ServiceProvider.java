package net.tvburger.sjawl.deploy.service;

import net.tvburger.sjawl.deploy.DeployException;

public interface ServiceProvider {

    default boolean hasService(Class<?> serviceTypeClass) throws DeployException {
        return hasService(serviceTypeClass, null);
    }

    boolean hasService(Class<?> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException;

    default <T> T getService(Class<T> serviceTypeClass) throws DeployException {
        return getService(serviceTypeClass, null);
    }

    <T> T getService(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException;

}
