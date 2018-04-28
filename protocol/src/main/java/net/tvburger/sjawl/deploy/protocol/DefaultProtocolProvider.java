package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.common.UnlimitedCache;
import net.tvburger.sjawl.config.ConfigurationProvider;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.distributed.SiteRegistry;
import net.tvburger.sjawl.deploy.distributed.mappers.ClassNameMapper;
import net.tvburger.sjawl.deploy.distributed.mappers.InstanceNameMapper;
import net.tvburger.sjawl.deploy.distributed.mappers.RemoteServiceRegistrationMapper;
import net.tvburger.sjawl.deploy.distributed.protocol.AddressSerializer;
import net.tvburger.sjawl.deploy.distributed.protocol.ServiceProxyFactory;
import net.tvburger.sjawl.deploy.distributed.remote.impl.RemoteLocalServicesStore;
import net.tvburger.sjawl.deploy.distributed.remote.impl.RemoteStoreSiteRegistry;
import net.tvburger.sjawl.deploy.distributed.spi.ProtocolProvider;
import net.tvburger.sjawl.deploy.distributed.spi.RemoteStoreProvider;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.client.ObjectStreamServiceProxyFactory;
import net.tvburger.sjawl.deploy.protocol.client.SiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.client.TcpSiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.config.ProtocolConfiguration;
import net.tvburger.sjawl.deploy.protocol.server.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class DefaultProtocolProvider implements ProtocolProvider<TcpAddress> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProtocolProvider.class);

    public static final class Factory implements ProtocolProvider.Factory<TcpAddress> {

        private final TcpAddressSerializer addressSerializer = new TcpAddressSerializer();

        public Factory() {
            LOGGER.debug("Factory instantiated");
        }

        @Override
        public TcpAddressSerializer getAddressSerializer() {
            return addressSerializer;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DefaultProtocolProvider create(RemoteStoreProvider<TcpAddress> remoteStoreProvider) throws DeployException {
            ServerContext serverContext = null;
            try {
                ProtocolConfiguration configuration = ConfigurationProvider.Singleton.get().getConfiguration(ProtocolConfiguration.class);
                Class<TcpAddress> addressTypeClass = TcpAddress.class;
                UUID siteId = configuration.getSiteId() != null ? configuration.getSiteId() : UUID.randomUUID();
                AddressSerializer<TcpAddress> addressSerializer = new TcpAddressSerializer();
                RemoteStoreSiteRegistry<TcpAddress> remoteSiteRegistry = new RemoteStoreSiteRegistry<>(remoteStoreProvider.getRemoteSitesStore());
                remoteSiteRegistry.init();
                SiteConnectionProvider<TcpAddress> siteConnectionProvider = new TcpSiteConnectionProvider();
                ServiceProxyFactory serviceProxyFactory = new ObjectStreamServiceProxyFactory<>(remoteSiteRegistry, siteConnectionProvider);
                RemoteServiceRegistrationMapper remoteServiceRegistryMapper = new RemoteServiceRegistrationMapper(
                        siteId, serviceProxyFactory, new ClassNameMapper(new UnlimitedCache<>()));
                LocalServicesStore localServicesStore = new RemoteLocalServicesStore(
                        remoteStoreProvider.getRemoteServicesStore(),
                        new ClassNameMapper(new UnlimitedCache<>()),
                        new InstanceNameMapper<ServiceDeploymentStrategy<?>>((Class) ServiceDeploymentStrategy.class, new UnlimitedCache<>()),
                        remoteServiceRegistryMapper);
                serverContext = ServerContext.Factory.create(configuration, localServicesStore, remoteSiteRegistry, siteId);
                TcpAddress address = serverContext.getAddress();
                return new DefaultProtocolProvider(
                        siteId,
                        address,
                        addressTypeClass,
                        addressSerializer,
                        remoteSiteRegistry,
                        localServicesStore,
                        serverContext);
            } catch (Throwable cause) {
                if (serverContext != null) {
                    serverContext.close();
                }
                throw new DeployException(cause);
            }
        }

    }

    private final UUID siteId;
    private final TcpAddress address;
    private final Class<TcpAddress> addressTypeClass;
    private final AddressSerializer<TcpAddress> addressSerializer;
    private final RemoteStoreSiteRegistry<TcpAddress> siteRegistry;
    private final LocalServicesStore localServicesStore;
    private final DeploymentContext serverContext;

    DefaultProtocolProvider(
            UUID siteId,
            TcpAddress address,
            Class<TcpAddress> addressTypeClass,
            AddressSerializer<TcpAddress> addressSerializer,
            RemoteStoreSiteRegistry<TcpAddress> siteRegistry,
            LocalServicesStore localServicesStore,
            DeploymentContext serverContext) {
        this.siteId = siteId;
        this.address = address;
        this.addressTypeClass = addressTypeClass;
        this.addressSerializer = addressSerializer;
        this.siteRegistry = siteRegistry;
        this.localServicesStore = localServicesStore;
        this.serverContext = serverContext;
    }

    @Override
    public UUID getSiteId() {
        return siteId;
    }

    @Override
    public TcpAddress getAddress() {
        return address;
    }

    @Override
    public Class<TcpAddress> getAddressTypeClass() {
        return addressTypeClass;
    }

    @Override
    public AddressSerializer<TcpAddress> getAddressSerializer() {
        return addressSerializer;
    }

    @Override
    public SiteRegistry<TcpAddress> getSiteRegistry() {
        return siteRegistry;
    }

    @Override
    public LocalServicesStore getLocalServicesStore() {
        return localServicesStore;
    }

    @Override
    public DeploymentContext getServerContext() {
        return serverContext;
    }

}
