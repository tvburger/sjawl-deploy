package net.tvburger.sjawl.deploy.zookeeper;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.config.ConfigurationException;
import net.tvburger.sjawl.config.ConfigurationProvider;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.mappers.ObjectBytesMapper;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.protocol.AddressSerializer;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteServicesStore;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteSitesStore;
import net.tvburger.sjawl.deploy.distributed.spi.RemoteStoreProvider;
import net.tvburger.sjawl.deploy.zookeeper.config.ZooKeeperConfiguration;

import java.io.IOException;

public final class ZooKeeperStoreProvider<A extends Address> implements RemoteStoreProvider<A> {

    public static final class Factory implements RemoteStoreProvider.Factory {

        private final ObjectBytesMapper mapper = new ObjectBytesMapper();

        public <A extends Address> RemoteStoreProvider<A> create(String deploymentId, AddressSerializer<A> serializer) throws DeployException {
            AssertUtil.assertNotNull(serializer);
            try {
                ZooKeeperConfiguration configuration = ConfigurationProvider.Singleton.get().getConfiguration(ZooKeeperConfiguration.class);
                ZooKeeperProvider provider = new ZooKeeperProvider(configuration.getConnectString(), configuration.getSessionTimeout());
                ZooKeeperServicesStore servicesStateManager = new ZooKeeperServicesStore(deploymentId, provider, mapper);
                ZooKeeperSitesStore<A> sitesStateManager = new ZooKeeperSitesStore<>(deploymentId, provider, serializer);
                sitesStateManager.init(configuration.getOnlyJoinExisting());
                servicesStateManager.init(configuration.getOnlyJoinExisting());
                return new ZooKeeperStoreProvider<>(servicesStateManager, sitesStateManager);
            } catch (ConfigurationException | IOException cause) {
                throw new DeployException(cause);
            }
        }

    }

    private final ZooKeeperServicesStore servicesStateManager;
    private final ZooKeeperSitesStore<A> sitesStateManager;

    public ZooKeeperStoreProvider(ZooKeeperServicesStore servicesStateManager, ZooKeeperSitesStore<A> sitesStateManager) {
        this.servicesStateManager = servicesStateManager;
        this.sitesStateManager = sitesStateManager;
    }

    @Override
    public RemoteSitesStore<A> getRemoteSitesStore() {
        return sitesStateManager;
    }

    @Override
    public RemoteServicesStore getRemoteServicesStore() {
        return servicesStateManager;
    }

}
