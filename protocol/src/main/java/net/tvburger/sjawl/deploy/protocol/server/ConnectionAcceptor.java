package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.SiteRegistrationManager;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public final class ConnectionAcceptor extends ManagedWorker implements AddressProvider, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionAcceptor.class);

    private final TcpAddress address;
    private final BlockingQueue<Socket> connectionQueue;
    private final SiteRegistrationManager<TcpAddress> siteRegistrationManager;
    private final UUID siteId;

    private volatile TcpAddress boundAddress;

    private ServerSocket serverSocket;

    public ConnectionAcceptor(TcpAddress address, BlockingQueue<Socket> connectionQueue, SiteRegistrationManager<TcpAddress> siteRegistrationManager, UUID siteId) {
        this.address = address;
        this.connectionQueue = connectionQueue;
        this.siteRegistrationManager = siteRegistrationManager;
        this.siteId = siteId;
        LOGGER.debug("ConnectorAcceptor instantiated with address: " + address);
    }

    @Override
    protected void performOneWorkUnit() {
        LOGGER.debug("performOneWorkUnit");
        try {
            LOGGER.debug("Waiting for new connection...");
            Socket socket = serverSocket.accept();
            LOGGER.info("Connection accepted: " + socket.getInetAddress() + ":" + socket.getPort());
            connectionQueue.add(socket);
        } catch (IOException cause) {
            LOGGER.debug("Accept connection was aborted due to: " + cause.getMessage());
            try {
                Thread.sleep(100);
            } catch (InterruptedException innerCause) {
                LOGGER.debug("Interrupted while waiting for 100ms recovery time...");
            }
        }
    }

    @Override
    public void activate() throws DeployException {
        LOGGER.debug("activate");
        try {
            serverSocket = new ServerSocket(address.getPort(), 10, address.getInetAddress());
            boundAddress = new TcpAddress(serverSocket.getInetAddress(), serverSocket.getLocalPort());
            LOGGER.info("activated at address: " + boundAddress);
            siteRegistrationManager.registerSite(siteId, boundAddress);
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public void started() {
        LOGGER.info("started");
    }

    @Override
    public void stopped() {
        LOGGER.info("stopped");
    }

    @Override
    public void deactivate() {
        LOGGER.debug("deactivate");
        try {
            close();
        } catch (DeployException cause) {
            LOGGER.warn("Failed to close connection: " + cause.getMessage());
        } finally {
            LOGGER.info("deactivated");
        }
    }

    @Override
    public void close() throws DeployException {
        LOGGER.debug("close");
        try {
            serverSocket.close();
        } catch (IOException cause) {
            throw new DeployException(cause);
        } finally {
            boundAddress = null;
            siteRegistrationManager.unregisterSite(siteId);
        }
    }

    @Override
    public TcpAddress getAddress() throws DeployException {
        return boundAddress;
    }

}