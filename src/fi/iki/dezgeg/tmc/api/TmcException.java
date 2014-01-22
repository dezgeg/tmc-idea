package fi.iki.dezgeg.tmc.api;

public class TmcException extends RuntimeException {
    public TmcException() {
    }

    public TmcException(String message) {
        super(message);
    }

    public TmcException(String message, Throwable cause) {
        super(message, cause);
    }

    public TmcException(Throwable cause) {
        super(cause);
    }

    public TmcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
