package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.deploy.protocol.TcpAddress;
import net.tvburger.sjawl.deploy.remote.Address;
import net.tvburger.sjawl.deploy.remote.SiteRegistry;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.UUID;

public final class ObjectStreamServiceProxy<A extends Address> implements InvocationHandler {

    private static final int NR_OR_RETRIES = 3;
    private static final int RETRY_DELAY = 1_000;

    private final SiteRegistry<A> siteRegistry;
    private final SiteConnectionProvider<A> provider;
    private final UUID siteId;
    private final UUID serviceRegistrationId;

    public ObjectStreamServiceProxy(SiteRegistry<A> siteRegistry, SiteConnectionProvider<A> provider, UUID siteId, UUID serviceRegistrationId) {
        this.siteRegistry = siteRegistry;
        this.provider = provider;
        this.siteId = siteId;
        this.serviceRegistrationId = serviceRegistrationId;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Exception lastCause = null;
        for (int i = 0; i < NR_OR_RETRIES; i++) {
            SiteConnection<A> connection = null;
            try {
                //A address = siteRegistry.getAddress(siteId);
                @SuppressWarnings("unchecked") A address = (A) new TcpAddress(InetAddress.getLoopbackAddress(), 8081); // TODO: revert back
                connection = provider.getSiteConnection(address);
                return connection.performRemoteCall(serviceRegistrationId, method, args);
            } catch (InvocationTargetException cause) {
                throw cause.getTargetException();
            } catch (Exception cause) {
                try {
                    System.err.println("Exception: " + cause.getMessage());
                    cause.printStackTrace();
                    lastCause = cause;
                    provider.resetConnection(connection);
                } catch (IOException innerCause) {
                    Thread.sleep(RETRY_DELAY);
                }
            }
        }
        throw new RemoteException("Failed to remote invoke method: " + method.getName(), lastCause);
    }

}
