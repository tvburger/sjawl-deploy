package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.RequestDTO;
import net.tvburger.sjawl.deploy.protocol.ResponseDTO;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public final class RequestHandler extends ManagedWorker {

    private final BlockingQueue<Socket> connectionQueue;
    private final LocalServicesStore store;

    public RequestHandler(BlockingQueue<Socket> connectionQueue, LocalServicesStore store) {
        this.connectionQueue = connectionQueue;
        this.store = store;
    }

    @Override
    protected void performOneWorkUnit() throws InterruptedException {
        ObjectOutputStream out = null;
        ResponseDTO response = null;
        Socket socket = null;
        try {
            socket = connectionQueue.take();
        } catch (InterruptedException cause) {
            return;
        }
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            RequestDTO request = (RequestDTO) in.readObject();
            ServiceRegistration<?> serviceRegistration = store.getServiceRegistration(request.getServiceRegistrationId());
            response = invokeCall(serviceRegistration.getServiceInstance(), request);
        } catch (IOException | DeployException | ClassNotFoundException cause) {
            response = new ResponseDTO(false, cause);
        } finally {
            if (out != null && response != null) {
                try {
                    out.writeObject(response);
                    connectionQueue.put(socket);
                } catch (IOException cause) {
                    try {
                        socket.close();
                    } catch (IOException innerCause) {
                    }
                }
            }
        }
    }

    private ResponseDTO invokeCall(Object serviceInstance, RequestDTO request) {
        try {
            Method method = serviceInstance.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object result = method.invoke(serviceInstance, request.getArguments());
            return new ResponseDTO(true, result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException cause) {
            return new ResponseDTO(false, cause);
        }
    }

    @Override
    public void deactivate() {
        System.out.println("deactivate");
    }

    @Override
    protected void stopped() throws DeployException {
        System.out.println("stopped: " + this);
    }

}
