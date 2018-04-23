package net.tvburger.sjawl.deploy.protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class ResponseDTO implements Serializable {

    private boolean success;
    private Object result;

    public ResponseDTO(boolean success, Object result) {
        this.success = success;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(success);
        out.writeObject(result);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        success = in.readBoolean();
        result = in.readObject();
    }

}
