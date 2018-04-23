package net.tvburger.sjawl.deploy;

public class DeployException extends Exception {

    public DeployException() {
    }

    public DeployException(String message) {
        super(message);
    }

    public DeployException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeployException(Throwable cause) {
        super(cause);
    }

}
