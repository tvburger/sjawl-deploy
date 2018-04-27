package net.tvburger.sjawl.deploy.zookeeper;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.protocol.Address;
import net.tvburger.sjawl.deploy.remote.protocol.AddressSerializer;
import net.tvburger.sjawl.deploy.remote.RemoteSitesStore;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class ZooKeeperSitesStore<A extends Address> extends AbstractZooKeeperStore implements RemoteSitesStore<A> {

    private final Set<Listener<A>> listeners = new CopyOnWriteArraySet<>(); // TODO: implement callbacks
    private final AddressSerializer<A> serializer;
    private final Object lock = new Object(); // TODO: fix lock

    public ZooKeeperSitesStore(String deploymentId, ZooKeeperProvider provider, AddressSerializer<A> serializer) {
        super(deploymentId, provider);
        this.serializer = serializer;
    }

    @Override
    public void addListener(Listener<A> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener<A> listener) {
        listeners.remove(listener);
    }

    @Override
    public Object getLock() {
        return lock;
    }

    @Override
    public A getSiteAddress(UUID siteId) throws DeployException, IOException {
        try {
            byte[] bytes = getZooKeeper().getData(getSitePath(siteId), false, null);
            return serializer.deserialize(bytes);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public Collection<UUID> getSiteIds() throws IOException {
        try {
            List<String> children = getZooKeeper().getChildren(getSitePath(), false);
            List<UUID> uuids = new ArrayList<>(children.size());
            for (String child : children) {
                uuids.add(UUID.fromString(child));
            }
            return uuids;
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void addSite(UUID siteId, A address) throws DeployException, IOException {
        try {
            getZooKeeper().create(getSitePath(siteId), serializer.serialize(address), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void updateSiteAddress(UUID siteId, A newAddress) throws DeployException, IOException {
        try {
            int version = getZooKeeper().exists(getSitePath(siteId), false).getVersion();
            getZooKeeper().setData(getSitePath(siteId), serializer.serialize(newAddress), version);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void removeSite(UUID siteId) throws IOException {
        try {
            int version = getZooKeeper().exists(getSitePath(siteId), false).getVersion();
            getZooKeeper().delete(getSitePath(siteId), version);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    public void init(boolean existingData) throws DeployException {
        try {
            super.init(existingData);
            if (getZooKeeper().exists(getSitePath(), false) == null) {
                if (existingData) {
                    throw new DeployException("No existing data found!");
                } else {
                    getZooKeeper().create(getSitePath(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
        } catch (InterruptedException | IOException | KeeperException cause) {
            throw new DeployException(cause);
        }
    }

    private String getSitePath() {
        return getDeploymentPath() + "/sites";
    }

    private String getSitePath(UUID siteId) {
        return getSitePath() + "/" + siteId.toString();
    }

}
