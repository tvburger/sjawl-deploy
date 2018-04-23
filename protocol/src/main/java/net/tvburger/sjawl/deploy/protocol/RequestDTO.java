package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.common.AssertUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

public final class RequestDTO implements Serializable {

    private UUID serviceRegistrationId;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] arguments;

    public RequestDTO() {
    }

    public RequestDTO(UUID serviceRegistrationId, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        AssertUtil.assertNotNull(serviceRegistrationId);
        AssertUtil.assertNotNull(methodName);
        this.serviceRegistrationId = serviceRegistrationId;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
    }

    public UUID getServiceRegistrationId() {
        return serviceRegistrationId;
    }

    public void setServiceRegistrationId(UUID serviceRegistrationId) {
        this.serviceRegistrationId = serviceRegistrationId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(serviceRegistrationId);
        out.writeUTF(methodName);
        if (parameterTypes == null) {
            out.writeInt(0);
        } else {
            out.writeInt(parameterTypes.length);
            for (Object parameterType : parameterTypes) {
                out.writeObject(parameterType);
            }
        }
        if (arguments == null) {
            out.writeInt(0);
        } else {
            out.writeInt(arguments.length);
            for (Object argument : arguments) {
                out.writeObject(argument);
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        serviceRegistrationId = (UUID) in.readObject();
        methodName = in.readUTF();
        int parameterTypesCount = in.readInt();
        if (parameterTypesCount == 0) {
            parameterTypes = null;
        } else {
            parameterTypes = new Class<?>[parameterTypesCount];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = (Class<?>) in.readObject();
            }
        }
        int argumentsCount = in.readInt();
        if (argumentsCount == 0) {
            arguments = null;
        } else {
            arguments = new Object[argumentsCount];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = in.readObject();
            }
        }
    }

}
