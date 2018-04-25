package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public final class ConnectionAcceptor extends ManagedWorker {

    private final TcpAddress address;
    private final BlockingQueue<Socket> connectionQueue;

    private ServerSocket serverSocket;

    public ConnectionAcceptor(TcpAddress address, BlockingQueue<Socket> connectionQueue) {
        this.address = address;
        this.connectionQueue = connectionQueue;
    }

    @Override
    protected void performOneWorkUnit() throws InterruptedException {
        try {
            connectionQueue.add(serverSocket.accept());
        } catch (IOException cause) {
            Thread.sleep(100);
        }
    }

    @Override
    public void activate() throws DeployException {
        try {
            serverSocket = new ServerSocket(address.getPort(), 10, address.getInetAddress());
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public void deactivate() {
        try {
            serverSocket.close();
        } catch (IOException cause) {
        }
    }

}
