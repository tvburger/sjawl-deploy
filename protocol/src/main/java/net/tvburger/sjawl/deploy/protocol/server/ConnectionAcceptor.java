package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.local.worker.AbstractWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public final class ConnectionAcceptor extends AbstractWorker {

    private final ServerSocket serverSocket;
    private final BlockingQueue<Socket> connectionQueue;

    public ConnectionAcceptor(ServerSocket serverSocket, BlockingQueue<Socket> connectionQueue) {
        this.serverSocket = serverSocket;
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
    public void deactivate() {
        try {
            serverSocket.close();
        } catch (IOException cause) {
        }
    }

}
