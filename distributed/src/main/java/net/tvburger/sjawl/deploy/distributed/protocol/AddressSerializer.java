package net.tvburger.sjawl.deploy.distributed.protocol;

import net.tvburger.sjawl.deploy.DeployException;

public interface AddressSerializer<A extends Address> {

    A deserialize(byte[] serializedAddress) throws DeployException;

    byte[] serialize(A address) throws DeployException;

}
