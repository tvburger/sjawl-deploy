package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.deploy.protocol.TcpAddress;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public final class TcpSiteConnectionProvider implements SiteConnectionProvider<TcpAddress> {

    private final Map<TcpAddress, SiteConnection<TcpAddress>> connections = new HashMap<>();

    @Override
    public SiteConnection<TcpAddress> getSiteConnection(TcpAddress address) throws IOException {
        SiteConnection<TcpAddress> siteConnection;
        boolean hasConnection;
        synchronized (connections) {
            hasConnection = connections.containsKey(address);
        }
        if (!hasConnection) {
            siteConnection = setupNewConnection(address);
            synchronized (connections) {
                connections.put(address, siteConnection);
            }
        } else {
            synchronized (connections) {
                siteConnection = connections.get(address);
            }
        }
        return siteConnection;
    }

    @Override
    public void resetConnection(SiteConnection<TcpAddress> connection) throws IOException {
        synchronized (connections) {
            TcpAddress address = connection.getAddress();
            connections.remove(address);
            try {
                connection.close();
            } catch (IOException cause) {
            }
        }
    }

    private SiteConnection<TcpAddress> setupNewConnection(TcpAddress address) throws IOException {
        try {
            Socket socket = new Socket(address.getInetAddress(), address.getPort());
            return new SiteConnection<>(address, SiteConnection.ObjectSocket.create(socket));
        } catch (IOException cause) {
            throw new IOException("Failed to setup connection to: " + address, cause);
        }
    }

}
