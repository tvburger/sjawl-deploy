package net.tvburger.sjawl.deploy.local.service;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public final class DefaultServicesAdministrator implements ServicesAdministrator {

    private final LocalServicesStateManager stateManager;

    public DefaultServicesAdministrator(LocalServicesStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public Collection<Class<?>> getRegisteredServiceTypes() throws DeployException {
        synchronized (stateManager.getLock()) {
            return new LinkedHashSet<>(stateManager.getServiceTypeClasses());
        }
    }

    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            return stateManager.getServiceTypeDeploymentStrategy(serviceTypeClass);
        }
    }

    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            return new ArrayList<>(stateManager.getServiceRegistrations(serviceTypeClass));
        }
    }

    @Override
    public <T> boolean isRegisteredServiceType(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            return stateManager.hasServiceType(serviceTypeClass);
        }
    }

    @Override
    public <T> void registerServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(deploymentStrategy);
        synchronized (stateManager.getLock()) {
            if (stateManager.hasServiceType(serviceTypeClass)) {
                throw new DeployException("Already serviceType defined for: " + serviceTypeClass.getName());
            }
            stateManager.addServiceType(serviceTypeClass, deploymentStrategy);
        }
    }

    @Override
    public <T> void unregisterServiceType(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            for (ServiceRegistration<T> serviceRegistration : getServiceRegistrations(serviceTypeClass)) {
                stateManager.removeServiceRegistration(serviceTypeClass, serviceRegistration);
            }
            stateManager.removeServiceType(serviceTypeClass);
        }
    }

}
