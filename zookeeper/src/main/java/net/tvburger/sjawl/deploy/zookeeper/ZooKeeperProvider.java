package net.tvburger.sjawl.deploy.zookeeper;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public final class ZooKeeperProvider {

    private CountDownLatch connectionLatch = new CountDownLatch(1);

    private final String connectString;
    private final int sessionTimeout;

    private ZooKeeper zooKeeper;

    public ZooKeeperProvider(String connectString, int sessionTimeout) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
    }

    private ZooKeeper createZooKeeper() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(connectString, sessionTimeout, we -> {
            if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectionLatch.countDown();
            }
            if (we.getState() == Watcher.Event.KeeperState.Expired) {
                reinit();
            }
        });

        connectionLatch.await();
        return zooKeeper;
    }

    private synchronized void reinit() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException cause) {
            }
        }
        zooKeeper = null;
    }

    public synchronized ZooKeeper getZooKeeper() throws IOException, InterruptedException {
        if (zooKeeper == null) {
            zooKeeper = createZooKeeper();
        }
        return zooKeeper;
    }

}
