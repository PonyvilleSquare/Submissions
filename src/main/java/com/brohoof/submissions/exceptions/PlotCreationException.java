package com.brohoof.submissions.exceptions;

public class PlotCreationException extends Exception {

    private static final long serialVersionUID = -219392532120095488L;

    public PlotCreationException() {
    }

    public PlotCreationException(String message) {
        super(message);
    }

    public PlotCreationException(Throwable cause) {
        super(cause);
    }

    public PlotCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
