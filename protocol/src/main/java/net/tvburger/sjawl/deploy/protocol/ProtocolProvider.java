package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.common.UnlimitedCache;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.client.ObjectStreamServiceProxyFactory;
import net.tvburger.sjawl.deploy.protocol.client.SiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.client.TcpSiteConnectionProvider;
import net.tvburger.sjawl.deploy.remote.RemoteProvider;
import net.tvburger.sjawl.deploy.remote.impl.RemoteLocalServicesStore;
import net.tvburger.sjawl.deploy.remote.impl.RemoteStateSiteRegistry;
import net.tvburger.sjawl.deploy.remote.mappers.ClassNameMapper;
import net.tvburger.sjawl.deploy.remote.mappers.InstanceNameMapper;
import net.tvburger.sjawl.deploy.remote.mappers.RemoteServiceRegistrationMapper;
import net.tvburger.sjawl.deploy.remote.protocol.AddressSerializer;
import net.tvburger.sjawl.deploy.remote.protocol.ServiceProxyFactory;

import java.util.UUID;

public final class ProtocolProvider {

    public static final class Factory {

        @SuppressWarnings("unchecked")
        public static ProtocolProvider create(UUID siteId, TcpAddress address, RemoteProvider.Factory remoteProviderFactory) throws DeployException {
            Class<TcpAddress> addressTypeClass = TcpAddress.class;
            AddressSerializer<TcpAddress> addressSerializer = new TcpAddressSerializer();
            RemoteProvider<TcpAddress> remoteProvider = remoteProviderFactory.create(addressSerializer);
            RemoteStateSiteRegistry<TcpAddress> siteRegistry = new RemoteStateSiteRegistry<>(remoteProvider.getRemoteSitesStore());
            siteRegistry.init(siteId, address);
            SiteConnectionProvider<TcpAddress> siteConnectionProvider = new TcpSiteConnectionProvider();
            ServiceProxyFactory serviceProxyFactory = new ObjectStreamServiceProxyFactory<>(siteRegistry, siteConnectionProvider);
            RemoteServiceRegistrationMapper remoteServiceRegistryMapper = new RemoteServiceRegistrationMapper(
                    siteId, serviceProxyFactory, new ClassNameMapper(new UnlimitedCache<>()));
            LocalServicesStore localServicesStore = new RemoteLocalServicesStore(
                    remoteProvider.getRemoteServicesStore(),
                    new ClassNameMapper(new UnlimitedCache<>()),
                    new InstanceNameMapper<ServiceDeploymentStrategy<?>>((Class) ServiceDeploymentStrategy.class, new UnlimitedCache<>()),
                    remoteServiceRegistryMapper);
            return new ProtocolProvider(
                    siteId,
                    address,
                    addressTypeClass,
                    addressSerializer,
                    remoteProvider,
                    siteRegistry,
                    siteConnectionProvider,
                    serviceProxyFactory,
                    remoteServiceRegistryMapper,
                    localServicesStore);
        }

        private Factory() {
        }

    }

    private final UUID siteId;
    private final TcpAddress address;
    private final Class<TcpAddress> addressTypeClass;
    private final AddressSerializer<TcpAddress> addressSerializer;
    private final RemoteProvider<TcpAddress> remoteProvider;
    private final RemoteStateSiteRegistry<TcpAddress> siteRegistry;
    private final SiteConnectionProvider<TcpAddress> siteConnectionProvider;
    private final ServiceProxyFactory serviceProxyFactory;
    private final RemoteServiceRegistrationMapper remoteServiceRegistrationMapper;
    private final LocalServicesStore localServicesStore;

    public ProtocolProvider(
            UUID siteId,
            TcpAddress address,
            Class<TcpAddress> addressTypeClass,
            AddressSerializer<TcpAddress> addressSerializer,
            RemoteProvider<TcpAddress> remoteProvider,
            RemoteStateSiteRegistry<TcpAddress> siteRegistry,
            SiteConnectionProvider<TcpAddress> siteConnectionProvider,
            ServiceProxyFactory serviceProxyFactory,
            RemoteServiceRegistrationMapper remoteServiceRegistrationMapper,
            LocalServicesStore localServicesStore) {
        this.siteId = siteId;
        this.address = address;
        this.addressTypeClass = addressTypeClass;
        this.addressSerializer = addressSerializer;
        this.remoteProvider = remoteProvider;
        this.siteRegistry = siteRegistry;
        this.siteConnectionProvider = siteConnectionProvider;
        this.serviceProxyFactory = serviceProxyFactory;
        this.remoteServiceRegistrationMapper = remoteServiceRegistrationMapper;
        this.localServicesStore = localServicesStore;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public TcpAddress getAddress() {
        return address;
    }

    public Class<TcpAddress> getAddressTypeClass() {
        return addressTypeClass;
    }

    public AddressSerializer<TcpAddress> getAddressSerializer() {
        return addressSerializer;
    }

    public RemoteProvider<TcpAddress> getRemoteProvider() {
        return remoteProvider;
    }

    public RemoteStateSiteRegistry<TcpAddress> getSiteRegistry() {
        return siteRegistry;
    }

    public SiteConnectionProvider<TcpAddress> getSiteConnectionProvider() {
        return siteConnectionProvider;
    }

    public ServiceProxyFactory getServiceProxyFactory() {
        return serviceProxyFactory;
    }

    public RemoteServiceRegistrationMapper getRemoteServiceRegistrationMapper() {
        return remoteServiceRegistrationMapper;
    }

    public LocalServicesStore getLocalServicesStore() {
        return localServicesStore;
    }

}
