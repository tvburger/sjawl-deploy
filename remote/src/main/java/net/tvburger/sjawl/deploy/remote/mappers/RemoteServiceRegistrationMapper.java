package net.tvburger.sjawl.deploy.remote.mappers;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.remote.RemoteServiceRegistration;
import net.tvburger.sjawl.deploy.remote.protocol.ServiceProxy;
import net.tvburger.sjawl.deploy.remote.protocol.ServiceProxyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RemoteServiceRegistrationMapper {

    private final Map<UUID, Object> idToInstanceMap = new HashMap<>();
    private final Map<Object, UUID> instanceToIdMap = new HashMap<>();

    private final UUID siteId;
    private final ServiceProxyFactory serviceProxyFactory;
    private final ClassNameMapper serviceTypeMapper;

    public RemoteServiceRegistrationMapper(
            UUID siteId, ServiceProxyFactory serviceProxyFactory, ClassNameMapper serviceTypeMapper) {
        this.siteId = siteId;
        this.serviceProxyFactory = serviceProxyFactory;
        this.serviceTypeMapper = serviceTypeMapper;
    }

    public <T> RemoteServiceRegistration toRemote(ServiceRegistration<T> serviceRegistration) throws DeployException {
        AssertUtil.assertNotNull(serviceRegistration);
        if (serviceRegistration.getServiceInstance() instanceof ServiceProxy) {
            throw new DeployException("Can't deploy service proxy, must be a local service!");
        }
        String serviceTypeName = serviceTypeMapper.toName(serviceRegistration.getServiceType());
        UUID serviceInstanceId;
        Object serviceInstance = serviceRegistration.getServiceInstance();
        if (instanceToIdMap.containsKey(serviceInstance)) {
            serviceInstanceId = instanceToIdMap.get(serviceInstance);
        } else {
            serviceInstanceId = UUID.randomUUID();
            instanceToIdMap.put(serviceInstance, serviceInstanceId);
            idToInstanceMap.put(serviceInstanceId, serviceInstance);
        }
        return new RemoteServiceRegistration(serviceRegistration.getRegistrationId(),
                serviceTypeName, serviceInstanceId, siteId, serviceRegistration.getServiceProperties());
    }

    @SuppressWarnings("unchecked")
    public <T> ServiceRegistration<T> fromRemote(RemoteServiceRegistration remoteServiceRegistration) throws DeployException {
        AssertUtil.assertNotNull(remoteServiceRegistration);
        Class<T> serviceTypeClass = (Class<T>) serviceTypeMapper.toClass(remoteServiceRegistration.getServiceType());
        T serviceInstance;
        UUID serviceId = remoteServiceRegistration.getServiceId();
        UUID serviceRegistrationId = remoteServiceRegistration.getRegistrationId();
        if (idToInstanceMap.containsKey(serviceId)) {
            serviceInstance = (T) idToInstanceMap.get(serviceId);
        } else if (isLocalService(remoteServiceRegistration)) {
            throw new DeployException("Can't find our own local service!");
        } else {
            UUID siteId = remoteServiceRegistration.getSiteId();
            serviceInstance = serviceProxyFactory.createServiceProxy(serviceTypeClass, siteId, serviceRegistrationId);
            idToInstanceMap.put(serviceId, serviceInstance);
            instanceToIdMap.put(serviceInstance, serviceId);
        }
        return new ServiceRegistration<>(remoteServiceRegistration.getRegistrationId(), serviceTypeClass, serviceInstance, remoteServiceRegistration.getServiceProperties());
    }

    private boolean isLocalService(RemoteServiceRegistration remoteServiceRegistration) {
        return siteId.equals(remoteServiceRegistration.getSiteId());
    }

}
