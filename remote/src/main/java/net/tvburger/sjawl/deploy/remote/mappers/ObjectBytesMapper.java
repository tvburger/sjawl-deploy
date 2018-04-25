package net.tvburger.sjawl.deploy.remote.mappers;

import java.io.*;

public final class ObjectBytesMapper {

    public byte[] toBytes(Serializable serializable) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(serializable);
            out.flush();
            return bos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T toObject(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (ClassNotFoundException cause) {
            throw new IOException(cause);
        }
    }

}
