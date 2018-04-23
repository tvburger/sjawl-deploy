package net.tvburger.sjawl.deploy.local.service;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.service.ServiceFilter;
import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

public final class DefaultServiceProvider implements ServiceProvider {

    private final LocalServicesStateManager stateManager;

    public DefaultServiceProvider(LocalServicesStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public boolean hasService(Class<?> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            return stateManager.hasServiceType(serviceTypeClass) && selectServiceRegistration(serviceTypeClass, serviceFilter) != null;
        }
    }

    @Override
    public <T> T getService(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            ServiceRegistration<T> serviceRegistration = selectServiceRegistration(serviceTypeClass, serviceFilter);
            if (serviceRegistration == null) {
                throw new DeployException("No service available!");
            }
            return serviceRegistration.getServiceInstance();
        }
    }

    private <T> ServiceRegistration<T> selectServiceRegistration(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        ServiceDeploymentStrategy<T> deploymentStrategy = stateManager.getServiceTypeDeploymentStrategy(serviceTypeClass);
        return deploymentStrategy.selectService(serviceTypeClass, stateManager.getServiceRegistrations(serviceTypeClass), serviceFilter);
    }

}
