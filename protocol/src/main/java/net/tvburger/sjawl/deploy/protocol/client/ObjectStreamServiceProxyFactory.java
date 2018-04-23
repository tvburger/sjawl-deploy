package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.remote.Address;
import net.tvburger.sjawl.deploy.remote.SiteRegistry;
import net.tvburger.sjawl.deploy.remote.service.ServiceProxyFactory;

import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.UUID;

public final class ObjectStreamServiceProxyFactory<A extends Address> implements ServiceProxyFactory {

    private final SiteRegistry<A> siteRegistry;
    private final SiteConnectionProvider<A> provider;

    public ObjectStreamServiceProxyFactory(SiteRegistry<A> siteRegistry, SiteConnectionProvider<A> provider) {
        this.siteRegistry = siteRegistry;
        this.provider = provider;
    }

    @Override
    public <R extends Remote> R createServiceProxy(Class<R> serviceTypeClass, UUID siteId, UUID serviceId) {
        if (!serviceTypeClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        AssertUtil.assertNotNull(siteId);
        AssertUtil.assertNotNull(serviceId);
        Class<?>[] parentInterface = serviceTypeClass.getInterfaces();
        Class<?>[] interfaces = new Class<?>[parentInterface.length + 1];
        for (int i = 0; i < parentInterface.length; i++) {
            interfaces[i] = parentInterface[i];
        }
        interfaces[interfaces.length - 1] = serviceTypeClass;
        return serviceTypeClass.cast(
                Proxy.newProxyInstance(
                        serviceTypeClass.getClassLoader(),
                        interfaces,
                        new ObjectStreamServiceProxy<>(siteRegistry, provider, siteId, serviceId)));
    }

}
