package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;

public interface AddressSerializer<A extends Address> {

    A deserialize(byte[] serializedAddress) throws DeployException;

    byte[] serialize(A address) throws DeployException;

}
