package top.mty.barklb.common;

public class BException extends Exception {
    public BException() {
        super();
    }

    public BException(String message) {
        super(message);
    }

    public BException(String message, Throwable cause) {
        super(message, cause);
    }

    public BException(Throwable cause) {
        super(cause);
    }
}

