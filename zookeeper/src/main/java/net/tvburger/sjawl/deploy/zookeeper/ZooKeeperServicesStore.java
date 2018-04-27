package net.tvburger.sjawl.deploy.zookeeper;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.RemoteServiceRegistration;
import net.tvburger.sjawl.deploy.remote.RemoteServicesStore;
import net.tvburger.sjawl.deploy.remote.mappers.ObjectBytesMapper;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public final class ZooKeeperServicesStore extends AbstractZooKeeperStore implements RemoteServicesStore {

    private final Set<RemoteServicesStore.Listener> listeners = new CopyOnWriteArraySet<>(); // TODO: implement callbacks
    private final ObjectBytesMapper mapper;
    private final Object lock = new Object(); // TODO: fix the lock

    public ZooKeeperServicesStore(String deploymentId, ZooKeeperProvider provider, ObjectBytesMapper mapper) {
        super(deploymentId, provider);
        this.mapper = mapper;
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public Object getLock() throws DeployException, IOException {
        return lock;
    }

    @Override
    public void addServiceType(String serviceTypeName, String deploymentStrategyName) throws DeployException, IOException {
        try {
            getZooKeeper().create(
                    getServicePath(serviceTypeName),
                    deploymentStrategyName.getBytes("UTF-8"),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void removeServiceType(String serviceTypeName) throws DeployException, IOException {
        try {
            int version = getZooKeeper().exists(getServicePath(serviceTypeName), false).getVersion();
            getZooKeeper().delete(getServicePath(serviceTypeName), version);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public boolean hasServiceType(String serviceTypeName) throws DeployException, IOException {
        try {
            return getZooKeeper().exists(getServicePath(serviceTypeName), false) != null;
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public Collection<String> getServiceTypes() throws DeployException, IOException {
        try {
            return Collections.unmodifiableList(getZooKeeper().getChildren(getServicePath(), false));
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public String getServiceDeploymentStrategy(String serviceTypeName) throws DeployException, IOException {
        try {
            byte[] bytes = getZooKeeper().getData(getServicePath(serviceTypeName), false, null);
            return new String(bytes, "UTF-8");
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void addServiceRegistration(RemoteServiceRegistration remoteServiceRegistration) throws DeployException, IOException {
        try {
            getZooKeeper().create(
                    getRegistrationPath(remoteServiceRegistration),
                    mapper.toBytes(remoteServiceRegistration.getServiceType()),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            getZooKeeper().create(
                    getServicePath(remoteServiceRegistration),
                    mapper.toBytes(remoteServiceRegistration),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException {
        try {
            int regVersion = getZooKeeper().exists(getRegistrationPath(serviceRegistrationId), false).getVersion();
            byte[] data = getZooKeeper().getData(getRegistrationPath(serviceRegistrationId), false, null);
            String serviceTypeName = mapper.toObject(data);
            int svcVersion = getZooKeeper().exists(getServicePath(serviceTypeName, serviceRegistrationId), false).getVersion();
            getZooKeeper().delete(getServicePath(serviceTypeName, serviceRegistrationId), svcVersion);
            getZooKeeper().delete(getRegistrationPath(serviceRegistrationId), regVersion);
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public boolean hasServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException {
        try {
            return getZooKeeper().exists(getRegistrationPath(serviceRegistrationId), false) != null;
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public RemoteServiceRegistration getServiceRegistration(UUID serviceRegistrationId) throws DeployException, IOException {
        try {
            byte[] data = getZooKeeper().getData(getRegistrationPath(serviceRegistrationId), false, null);
            String serviceTypeName = mapper.toObject(data);
            return getServiceRegistration(serviceTypeName, serviceRegistrationId.toString());
        } catch (InterruptedException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    private RemoteServiceRegistration getServiceRegistration(String serviceTypeName, String childName) throws DeployException, IOException {
        try {
            byte[] bytes = getZooKeeper().getData(getServicePath(serviceTypeName) + "/" + childName, false, null);
            return mapper.toObject(bytes);
        } catch (InterruptedException | IllegalArgumentException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public Collection<RemoteServiceRegistration> getServiceRegistrations(String serviceTypeName) throws DeployException, IOException {
        try {
            List<RemoteServiceRegistration> registrations = new LinkedList<>();
            List<String> children = getZooKeeper().getChildren(getServicePath(serviceTypeName), false, null);
            for (String child : children) {
                registrations.add(getServiceRegistration(serviceTypeName, child));
            }
            return registrations;
        } catch (InterruptedException | IllegalArgumentException | KeeperException cause) {
            throw new IOException(cause);
        }
    }

    public void init(boolean existingData) throws DeployException {
        try {
            super.init(existingData);
            if (getZooKeeper().exists(getServicePath(), false) == null) {
                if (existingData) {
                    throw new DeployException("No existing data found!");
                } else {
                    getZooKeeper().create(getServicePath(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
            if (getZooKeeper().exists(getRegistrationPath(), false) == null) {
                if (existingData) {
                    throw new DeployException("No existing data found!");
                } else {
                    getZooKeeper().create(getRegistrationPath(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }
        } catch (InterruptedException | IOException | KeeperException cause) {
            throw new DeployException(cause);
        }
    }

    private String getServicePath() {
        return getDeploymentPath() + "/services";
    }

    private String getRegistrationPath() {
        return getDeploymentPath() + "/services_index";
    }

    private String getRegistrationPath(UUID serviceRegistrationId) {
        return getRegistrationPath() + "/" + serviceRegistrationId.toString();
    }

    private String getRegistrationPath(RemoteServiceRegistration remoteServiceRegistration) {
        return getRegistrationPath(remoteServiceRegistration.getRegistrationId());
    }

    private String getServicePath(String serviceTypeName) {
        return getServicePath() + "/" + serviceTypeName;
    }

    private String getServicePath(String serviceTypeName, UUID serviceRegistrationId) {
        return getServicePath(serviceTypeName + "/" + serviceRegistrationId);
    }

    private String getServicePath(RemoteServiceRegistration remoteServiceRegistration) {
        return getServicePath(remoteServiceRegistration.getServiceType(), remoteServiceRegistration.getRegistrationId());
    }

}
