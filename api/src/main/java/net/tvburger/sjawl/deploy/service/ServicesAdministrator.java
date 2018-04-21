package net.tvburger.sjawl.deploy.service;

import net.tvburger.sjawl.deploy.DeployException;

import java.util.Collection;

public interface ServicesAdministrator {

    Collection<Class<?>> getRegisteredServiceTypes() throws DeployException;

    <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException;

    <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException;

    <T> boolean isRegisteredServiceType(Class<T> serviceTypeClass) throws DeployException;

    <T> void registerServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException;

    <T> void unregisterServiceType(Class<T> serviceTypeClass) throws DeployException;

}
