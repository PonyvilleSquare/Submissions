package com.brohoof.submissions.exceptions;

public class RentLoadingException extends Exception {

    private static final long serialVersionUID = -2283584921685146138L;

    public RentLoadingException() {
    }

    public RentLoadingException(String message) {
        super(message);
    }

    public RentLoadingException(Throwable cause) {
        super(cause);
    }

    public RentLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
