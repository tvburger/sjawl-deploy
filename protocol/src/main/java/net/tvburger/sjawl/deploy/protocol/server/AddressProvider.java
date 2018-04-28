package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;

public interface AddressProvider {

    TcpAddress getAddress() throws DeployException;

}
