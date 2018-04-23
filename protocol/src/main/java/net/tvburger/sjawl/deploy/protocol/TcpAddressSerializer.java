package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.AddressSerializer;

import java.io.IOException;
import java.net.InetAddress;

public final class TcpAddressSerializer implements AddressSerializer<TcpAddress> {

    @Override
    public TcpAddress deserialize(byte[] serializedAddress) throws DeployException {
        try {
            String tcpAddressString = new String(serializedAddress, "UTF-8");
            String[] parts = tcpAddressString.split("@");
            if (parts.length != 2) {
                throw new IOException("Invalid bytes!");
            }
            InetAddress inetAddress = InetAddress.getByName(parts[0]);
            int port = Integer.parseInt(parts[1]);
            return new TcpAddress(inetAddress, port);
        } catch (IllegalArgumentException | IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public byte[] serialize(TcpAddress tcpAddress) throws DeployException {
        try {
            String string = tcpAddress.getInetAddress().getCanonicalHostName() + "@" + tcpAddress.getPort();
            return string.getBytes("UTF-8");
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

}
