package net.tvburger.sjawl.deploy;

public class DeployException extends Exception {

    private final Object service;

    public DeployException(Object service) {
        this.service = service;
    }

    public DeployException(String message, Object service) {
        super(message);
        this.service = service;
    }

    public DeployException(String message, Throwable cause, Object service) {
        super(message, cause);
        this.service = service;
    }

    public DeployException(Throwable cause, Object service) {
        super(cause);
        this.service = service;
    }

}
