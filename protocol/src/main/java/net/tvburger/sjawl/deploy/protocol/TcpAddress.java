package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.deploy.remote.Address;

import java.net.InetAddress;

public final class TcpAddress implements Address {

    private final InetAddress inetAddress;
    private final int port;

    public TcpAddress(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

}
