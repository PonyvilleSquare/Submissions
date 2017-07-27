package com.brohoof.submissions.exceptions;

public class PlotLoadingException extends Exception {

    private static final long serialVersionUID = 4841239039070389795L;

    public PlotLoadingException() {
    }

    public PlotLoadingException(String message) {
        super(message);
    }

    public PlotLoadingException(Throwable cause) {
        super(cause);
    }

    public PlotLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
