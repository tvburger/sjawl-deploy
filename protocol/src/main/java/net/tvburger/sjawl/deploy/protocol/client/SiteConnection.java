package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.deploy.protocol.RequestDTO;
import net.tvburger.sjawl.deploy.protocol.ResponseDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.UUID;

public final class SiteConnection<A> implements AutoCloseable {

    public static class ObjectSocket implements AutoCloseable {

        private final Socket socket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        public static ObjectSocket create(Socket socket) throws IOException {
            return new ObjectSocket(socket,
                    new ObjectInputStream(socket.getInputStream()),
                    new ObjectOutputStream(socket.getOutputStream()));
        }

        public ObjectSocket(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        public Socket getSocket() {
            return socket;
        }

        public ObjectInputStream getIn() {
            return in;
        }

        public ObjectOutputStream getOut() {
            return out;
        }

        @Override
        public void close() throws IOException {
            socket.close();
            in.close();
            out.close();
        }

    }

    private final A address;
    private final ObjectSocket objectSocket;

    public SiteConnection(A address, ObjectSocket objectSocket) {
        this.address = address;
        this.objectSocket = objectSocket;
    }

    public A getAddress() {
        return address;
    }

    public Object performRemoteCall(UUID serviceRegistrationId, Method method, Object[] args) throws IOException, InvocationTargetException {
        RequestDTO request = new RequestDTO(serviceRegistrationId, method.getName(), method.getParameterTypes(), args);
        try {
            objectSocket.getOut().writeObject(request);
            objectSocket.getOut().flush();
            ResponseDTO response = (ResponseDTO) objectSocket.getIn().readObject();
            if (!response.isSuccess()) {
                throw new InvocationTargetException((Throwable) response.getResult());
            }
            return response.getResult();
        } catch (ClassNotFoundException cause) {
            throw new IOException(cause);
        }
    }

    @Override
    public void close() throws IOException {
        objectSocket.close();
    }

}
