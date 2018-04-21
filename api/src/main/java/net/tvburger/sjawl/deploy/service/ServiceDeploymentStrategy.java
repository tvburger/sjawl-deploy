package net.tvburger.sjawl.deploy.service;

import net.tvburger.sjawl.deploy.DeployException;

import java.util.Collection;

public interface ServiceDeploymentStrategy<T> {

    ServiceRegistration<T> selectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> serviceRegistrations, ServiceFilter serviceFilter) throws DeployException;

}
