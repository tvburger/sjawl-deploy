package net.tvburger.sjawl.deploy.zookeeper.config;

import net.tvburger.sjawl.config.Configuration;

public class ZooKeeperConfiguration implements Configuration {

    private final String connectString;
    private final int sessionTimeout;
    private boolean onlyJoinExisting;

    public ZooKeeperConfiguration(String connectString, int sessionTimeout, boolean onlyJoinExisting) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
        this.onlyJoinExisting = onlyJoinExisting;
    }

    public String getConnectString() {
        return connectString;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public boolean getOnlyJoinExisting() {
        return onlyJoinExisting;
    }
}
