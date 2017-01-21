package com.brohoof.submissions.exceptions;

public class PlotLoadException extends Exception {

    private static final long serialVersionUID = 4841239039070389795L;

    public PlotLoadException() {
    }

    public PlotLoadException(String message) {
        super(message);
    }

    public PlotLoadException(Throwable cause) {
        super(cause);
    }

    public PlotLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
