package net.tvburger.sjawl.deploy.local.impl;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public final class DefaultServicesAdministrator implements ServicesAdministrator {

    private final LocalServicesStore store;

    public DefaultServicesAdministrator(LocalServicesStore store) {
        this.store = store;
    }

    @Override
    public Collection<Class<?>> getRegisteredServiceTypes() throws DeployException {
        synchronized (store.getLock()) {
            return new LinkedHashSet<>(store.getServiceTypes());
        }
    }

    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            store.assertRegistered(serviceTypeClass);
            return store.getServiceDeploymentStrategy(serviceTypeClass);
        }
    }

    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            store.assertRegistered(serviceTypeClass);
            return new ArrayList<>(store.getServiceRegistrations(serviceTypeClass));
        }
    }

    @Override
    public <T> boolean isRegisteredServiceType(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            return store.hasServiceType(serviceTypeClass);
        }
    }

    @Override
    public <T> void registerServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(deploymentStrategy);
        synchronized (store.getLock()) {
            if (store.hasServiceType(serviceTypeClass)) {
                throw new DeployException("Already serviceType defined for: " + serviceTypeClass.getName());
            }
            store.addServiceType(serviceTypeClass, deploymentStrategy);
        }
    }

    @Override
    public <T> void unregisterServiceType(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            store.assertRegistered(serviceTypeClass);
            for (ServiceRegistration<T> serviceRegistration : getServiceRegistrations(serviceTypeClass)) {
                store.removeServiceRegistration(serviceRegistration.getRegistrationId());
            }
            store.removeServiceType(serviceTypeClass);
        }
    }

}
