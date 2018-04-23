package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.local.worker.AbstractWorker;
import net.tvburger.sjawl.deploy.protocol.RequestDTO;
import net.tvburger.sjawl.deploy.protocol.ResponseDTO;
import net.tvburger.sjawl.deploy.remote.service.ServiceRegistrationRegistry;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

// TODO: don't use a TCP connection for just a single request...
public final class RequestHandler extends AbstractWorker {

    private final BlockingQueue<Socket> connectionQueue;
    private final ServiceRegistrationRegistry registry;

    public RequestHandler(BlockingQueue<Socket> connectionQueue, ServiceRegistrationRegistry registry) {
        this.connectionQueue = connectionQueue;
        this.registry = registry;
    }

    @Override
    protected void performOneWorkUnit() throws InterruptedException {
        ObjectOutputStream out = null;
        ResponseDTO response = null;
        Socket socket = connectionQueue.take();
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            RequestDTO request = (RequestDTO) in.readObject();
            ServiceRegistration<?> serviceRegistration = registry.getServiceRegistration(request.getServiceRegistrationId());
            response = invokeCall(serviceRegistration.getServiceInstance(), request);
        } catch (IOException | DeployException | ClassNotFoundException cause) {
            response = new ResponseDTO(false, cause);
        } finally {
            if (out != null && response != null) {
                try {
                    out.writeObject(response);
                } catch (IOException cause) {
                } finally {
                    try {
                        socket.close();
                    } catch (IOException cause) {
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

}
