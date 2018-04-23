package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.rmi.Remote;
import java.util.UUID;

public final class RemoteServiceRegistrationMapper {

    private final UUID siteId;
    private final ServiceProxyFactory serviceProxyFactory;
    private final ServiceRegistrationRegistry localServiceRegistry;
    private final ServiceRegistrationRegistry proxyServiceRegistry;

    public RemoteServiceRegistrationMapper(UUID siteId, ServiceProxyFactory serviceProxyFactory, ServiceRegistrationRegistry localServiceRegistry, ServiceRegistrationRegistry proxyServiceRegistry) {
        this.siteId = siteId;
        this.serviceProxyFactory = serviceProxyFactory;
        this.localServiceRegistry = localServiceRegistry;
        this.proxyServiceRegistry = proxyServiceRegistry;
    }

    public <T> RemoteServiceRegistration toRemote(ServiceRegistration<T> serviceRegistration) throws DeployException {
        UUID serviceRegistrationId;
        if (localServiceRegistry.hasServiceRegistration(serviceRegistration)) {
            serviceRegistrationId = localServiceRegistry.addServiceRegistration(serviceRegistration);
        } else {
            serviceRegistrationId = localServiceRegistry.getServiceRegistrationId(serviceRegistration);
        }
        return new RemoteServiceRegistration(serviceRegistrationId, siteId, serviceRegistration.getServiceProperties());
    }

    public <T, R extends Remote> ServiceRegistration<T> fromRemote(Class<R> remoteServiceTypeClass, RemoteServiceRegistration remoteServiceRegistration) throws DeployException {
        ServiceRegistration<T> serviceRegistration;
        if (isLocalService(remoteServiceRegistration)) {
            serviceRegistration = localServiceRegistry.getServiceRegistration(remoteServiceRegistration.getServiceRegistrationId());
        } else if (proxyServiceRegistry.hasServiceRegistration(remoteServiceRegistration.getServiceRegistrationId())) {
            serviceRegistration = proxyServiceRegistry.getServiceRegistration(remoteServiceRegistration.getServiceRegistrationId());
        } else {
            serviceRegistration = createServiceRegistration(remoteServiceTypeClass, remoteServiceRegistration);
            proxyServiceRegistry.addServiceRegistration(serviceRegistration, remoteServiceRegistration.getServiceRegistrationId());
        }
        return serviceRegistration;
    }

    private boolean isLocalService(RemoteServiceRegistration remoteServiceRegistration) {
        return siteId.equals(remoteServiceRegistration.getSiteId());
    }

    @SuppressWarnings("unchecked")
    private <T, R extends Remote> ServiceRegistration<T> createServiceRegistration(Class<R> remoteServiceTypeClass, RemoteServiceRegistration remoteServiceRegistration) throws DeployException {
        UUID serviceId = remoteServiceRegistration.getServiceRegistrationId();
        UUID siteId = remoteServiceRegistration.getSiteId();
        T serviceInstance = (T) serviceProxyFactory.createServiceProxy(remoteServiceTypeClass, siteId, serviceId);
        return new ServiceRegistration<>(serviceInstance, remoteServiceRegistration.getServiceProperties());
    }

}
