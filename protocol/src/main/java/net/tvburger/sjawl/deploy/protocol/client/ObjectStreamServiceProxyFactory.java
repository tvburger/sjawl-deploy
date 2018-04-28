package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.distributed.remote.impl.RemoteStoreSiteRegistry;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.protocol.ServiceProxy;
import net.tvburger.sjawl.deploy.distributed.protocol.ServiceProxyFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

public final class ObjectStreamServiceProxyFactory<A extends Address> implements ServiceProxyFactory {

    private final RemoteStoreSiteRegistry<A> siteRegistry;
    private final SiteConnectionProvider<A> provider;

    public ObjectStreamServiceProxyFactory(RemoteStoreSiteRegistry<A> siteRegistry, SiteConnectionProvider<A> provider) {
        this.siteRegistry = siteRegistry;
        this.provider = provider;
    }

    @Override
    public <T> T createServiceProxy(Class<T> serviceTypeClass, UUID siteId, UUID serviceRegistrationId) {
        if (!serviceTypeClass.isInterface()) {
            throw new IllegalArgumentException("Must be an interface!");
        }
        if (ServiceProxy.class.isAssignableFrom(serviceTypeClass)) {
            throw new IllegalArgumentException("We refuse to proxy a proxy!");
        }
        AssertUtil.assertNotNull(siteId);
        AssertUtil.assertNotNull(serviceRegistrationId);
        Class<?>[] parentInterface = serviceTypeClass.getInterfaces();
        Class<?>[] interfaces = new Class<?>[parentInterface.length + 2];
        for (int i = 0; i < parentInterface.length; i++) {
            interfaces[i] = parentInterface[i];
        }
        interfaces[interfaces.length - 2] = ServiceProxy.class;
        interfaces[interfaces.length - 1] = serviceTypeClass;
        return serviceTypeClass.cast(
                Proxy.newProxyInstance(
                        serviceTypeClass.getClassLoader(),
                        interfaces,
                        new ObjectStreamServiceProxy<>(siteRegistry, provider, siteId, serviceRegistrationId)));
    }

}
