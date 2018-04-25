package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.remote.impl.RemoteStateSiteRegistry;
import net.tvburger.sjawl.deploy.remote.protocol.Address;
import net.tvburger.sjawl.deploy.remote.protocol.ServiceProxy;
import net.tvburger.sjawl.deploy.remote.protocol.ServiceProxyFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

public final class ObjectStreamServiceProxyFactory<A extends Address> implements ServiceProxyFactory {

    private final RemoteStateSiteRegistry<A> siteRegistry;
    private final SiteConnectionProvider<A> provider;

    public ObjectStreamServiceProxyFactory(RemoteStateSiteRegistry<A> siteRegistry, SiteConnectionProvider<A> provider) {
        this.siteRegistry = siteRegistry;
        this.provider = provider;
    }

    @Override
    public <T> T createServiceProxy(Class<T> serviceTypeClass, UUID siteId, UUID serviceId) {
        if (!serviceTypeClass.isInterface()) {
            throw new IllegalArgumentException("Must be an interface!");
        }
        if (ServiceProxy.class.isAssignableFrom(serviceTypeClass)) {
            throw new IllegalArgumentException("We refuse to proxy a proxy!");
        }
        AssertUtil.assertNotNull(siteId);
        AssertUtil.assertNotNull(serviceId);
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
                        new ObjectStreamServiceProxy<>(siteRegistry, provider, siteId, serviceId)));
    }

}
