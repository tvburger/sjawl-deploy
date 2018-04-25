package net.tvburger.sjawl.deploy;

import net.tvburger.sjawl.deploy.admin.ServiceRegistration;

import java.util.Collection;

public interface ServiceDeploymentStrategy<T> {

    ServiceRegistration<T> selectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> serviceRegistrations, ServiceFilter serviceFilter) throws DeployException;

}
