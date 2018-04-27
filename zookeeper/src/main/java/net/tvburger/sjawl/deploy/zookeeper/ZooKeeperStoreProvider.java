package net.tvburger.sjawl.deploy.zookeeper;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.RemoteProvider;
import net.tvburger.sjawl.deploy.remote.RemoteServicesStore;
import net.tvburger.sjawl.deploy.remote.RemoteSitesStore;
import net.tvburger.sjawl.deploy.remote.mappers.ObjectBytesMapper;
import net.tvburger.sjawl.deploy.remote.protocol.Address;
import net.tvburger.sjawl.deploy.remote.protocol.AddressSerializer;

public final class ZooKeeperStoreProvider<A extends Address> implements RemoteProvider<A> {

    public static final class Factory implements RemoteProvider.Factory {

        public static Factory get(String deploymentId, boolean existingData) {
            return new Builder().withDeploymentId(deploymentId).withExistingData(existingData).build();
        }

        public static final class Builder {

            private String deploymentId;
            private String connectString = "localhost";
            private int sessionTimeout = 2_000;
            private boolean existingData;

            public Builder withDeploymentId(String deploymentId) {
                this.deploymentId = deploymentId;
                return this;
            }

            public Builder withExistingData(boolean existingData) {
                this.existingData = existingData;
                return this;
            }

            public void validate() {
                if (deploymentId == null || deploymentId.isEmpty()) {
                    throw new IllegalStateException();
                }
            }

            public Factory build() {
                validate();
                return new Factory(deploymentId, new ZooKeeperProvider(connectString, sessionTimeout), existingData);
            }

        }

        private final ObjectBytesMapper mapper = new ObjectBytesMapper();

        private final String deploymentId;
        private final ZooKeeperProvider provider;
        private final boolean existingData;

        protected Factory(String deploymentId, ZooKeeperProvider provider, boolean existingData) {
            this.deploymentId = deploymentId;
            this.provider = provider;
            this.existingData = existingData;
        }

        public <A extends Address> RemoteProvider<A> create(AddressSerializer<A> serializer) throws DeployException {
            AssertUtil.assertNotNull(serializer);
            ZooKeeperServicesStore servicesStateManager = new ZooKeeperServicesStore(deploymentId, provider, mapper);
            ZooKeeperSitesStore<A> sitesStateManager = new ZooKeeperSitesStore<>(deploymentId, provider, serializer);
            sitesStateManager.init(existingData);
            servicesStateManager.init(existingData);
            return new ZooKeeperStoreProvider<>(servicesStateManager, sitesStateManager);
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
