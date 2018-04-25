package net.tvburger.sjawl.deploy.local.impl;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceProperties;
import net.tvburger.sjawl.deploy.ServiceRegistry;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.utils.ServiceRegistrationRegistry;

import java.util.UUID;

public final class DefaultServiceRegistry implements ServiceRegistry {

    public static final class Factory {

        public static DefaultServiceRegistry create(LocalServicesStore store) {
            AssertUtil.assertNotNull(store);
            return new DefaultServiceRegistry(new ServiceRegistrationRegistry(), store);
        }

        private Factory() {
        }

    }

    private final ServiceRegistrationRegistry registry;
    private final LocalServicesStore store;

    protected DefaultServiceRegistry(ServiceRegistrationRegistry registry, LocalServicesStore store) {
        this.registry = registry;
        this.store = store;
    }

    @Override
    public <T> UUID registerService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (store.getLock()) {
            store.assertRegistered(serviceTypeClass);
            UUID serviceRegistrationId = UUID.randomUUID();
            ServiceRegistration<T> serviceRegistration = new ServiceRegistration<>(serviceRegistrationId, serviceTypeClass, serviceInstance, serviceProperties);
            store.addServiceRegistration(serviceRegistration);
            registry.addServiceRegistration(serviceRegistration);
            return serviceRegistrationId;
        }
    }

    @Override
    public void unregisterService(UUID serviceRegistrationId) throws DeployException {
        AssertUtil.assertNotNull(serviceRegistrationId);
        synchronized (store.getLock()) {
            if (!registry.hasServiceRegistration(serviceRegistrationId)) {
                throw new DeployException("No such service is registered!");
            }
            store.removeServiceRegistration(serviceRegistrationId);
            registry.removeServiceRegistration(serviceRegistrationId);
        }
    }

    @Override
    public <T> void unregisterService(T serviceInstance) throws DeployException {
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (store.getLock()) {
            for (UUID serviceRegistrationId : registry.getServiceRegistrationIds(serviceInstance)) {
                store.removeServiceRegistration(serviceRegistrationId);
                registry.removeServiceRegistration(serviceRegistrationId);
            }
        }
    }

}
