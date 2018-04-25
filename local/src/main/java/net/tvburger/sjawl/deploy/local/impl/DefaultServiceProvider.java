package net.tvburger.sjawl.deploy.local.impl;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.ServiceFilter;
import net.tvburger.sjawl.deploy.ServiceProvider;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;

public final class DefaultServiceProvider implements ServiceProvider {

    private final LocalServicesStore store;

    public DefaultServiceProvider(LocalServicesStore store) {
        this.store = store;
    }

    @Override
    public boolean hasService(Class<?> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            return store.hasServiceType(serviceTypeClass) && selectServiceRegistration(serviceTypeClass, serviceFilter) != null;
        }
    }

    @Override
    public <T> T getService(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (store.getLock()) {
            store.assertRegistered(serviceTypeClass);
            ServiceRegistration<T> serviceRegistration = selectServiceRegistration(serviceTypeClass, serviceFilter);
            if (serviceRegistration == null) {
                throw new DeployException("No service available!");
            }
            return serviceRegistration.getServiceInstance();
        }
    }

    private <T> ServiceRegistration<T> selectServiceRegistration(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        ServiceDeploymentStrategy<T> deploymentStrategy = store.getServiceDeploymentStrategy(serviceTypeClass);
        return deploymentStrategy.selectService(serviceTypeClass, store.getServiceRegistrations(serviceTypeClass), serviceFilter);
    }

}
