package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.RequestDTO;
import net.tvburger.sjawl.deploy.protocol.ResponseDTO;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public final class RequestHandler extends ManagedWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

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
        Socket socket = getSocket();
        if (socket == null) {
            return;
        }
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            RequestDTO request = (RequestDTO) in.readObject();
            LOGGER.debug("Looking up service registration: " + request.getServiceRegistrationId());
            ServiceRegistration<?> serviceRegistration = store.getServiceRegistration(request.getServiceRegistrationId());
            LOGGER.debug(String.format("Service registration maps to %s -> %s",
                    request.getServiceRegistrationId(), serviceRegistration.getServiceInstance()));
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

    private Socket getSocket() {
        try {
            LOGGER.debug("Waiting for connection...");
            Socket socket = connectionQueue.take();
            LOGGER.debug("Got connection: " + socket);
            return socket;
        } catch (InterruptedException cause) {
            LOGGER.debug("We got interrupted!");
            return null;
        }
    }

    private ResponseDTO invokeCall(Object serviceInstance, RequestDTO request) {
        try {
            Method method = serviceInstance.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            LOGGER.debug(String.format("Invoking method: (%s, %s, %s)",
                    serviceInstance, request.getMethodName(), Arrays.toString(request.getArguments())));
            Object result = method.invoke(serviceInstance, request.getArguments());
            return new ResponseDTO(true, result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException cause) {
            return new ResponseDTO(false, cause);
        }
    }

}
