package net.tvburger.sjawl.deploy.zookeeper;

import net.tvburger.sjawl.deploy.DeployException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public abstract class AbstractZooKeeperStore {

    public static final String ROOT_PATH = "/net.tvburger.sjawl.deploy";

    private final String deploymentId;
    private final ZooKeeperProvider provider;

    public AbstractZooKeeperStore(String deploymentId, ZooKeeperProvider provider) {
        this.deploymentId = deploymentId;
        this.provider = provider;
    }

    public void init(boolean existingData) throws DeployException {
        try {
            if (provider.getZooKeeper().exists(getDeploymentPath(), false) == null) {
                if (existingData) {
                    throw new DeployException("No existing data found!");
                } else {
                    if (provider.getZooKeeper().exists(ROOT_PATH, false) == null) {
                        provider.getZooKeeper().create(ROOT_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                    provider.getZooKeeper().create(getDeploymentPath(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
        } catch (InterruptedException | IOException | KeeperException cause) {
            throw new DeployException(cause);
        }
    }

    protected final String getDeploymentId() {
        return deploymentId;
    }

    protected final ZooKeeperProvider getProvider() {
        return provider;
    }

    protected final ZooKeeper getZooKeeper() throws IOException {
        try {
            return provider.getZooKeeper();
        } catch (InterruptedException | IOException cause) {
            throw new IOException("Failed to communicate with ZooKeeper: " + cause.getMessage(), cause);
        }
    }

    protected final String getDeploymentPath() {
        return ROOT_PATH + "/" + deploymentId;
    }

}
