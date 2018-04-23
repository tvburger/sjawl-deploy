package net.tvburger.sjawl.deploy.local.service;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceProperties;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;

import java.util.Objects;

public final class DefaultServiceRegistry implements ServiceRegistry {

    private final LocalServicesStateManager stateManager;

    public DefaultServiceRegistry(LocalServicesStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public <T> boolean isRegistered(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            for (ServiceRegistration<?> serviceRegistration : stateManager.getServiceRegistrations(serviceTypeClass)) {
                if (serviceRegistration.getServiceInstance().equals(serviceInstance) &&
                        Objects.equals(serviceRegistration.getServiceProperties(), serviceProperties)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public <T> void registerService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            stateManager.addServiceRegistration(serviceTypeClass, new ServiceRegistration<>(serviceInstance, serviceProperties));
        }
    }

    @Override
    public <T> void unregisterService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (stateManager.getLock()) {
            stateManager.assertRegistered(serviceTypeClass);
            if (!isRegistered(serviceTypeClass, serviceInstance, serviceProperties)) {
                throw new DeployException("No such service is registered!");
            }
            stateManager.removeServiceRegistration(serviceTypeClass, new ServiceRegistration<>(serviceInstance, serviceProperties));
        }
    }

}
