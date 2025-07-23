package com.cashmallow.api.domain.shared;

public class CashmallowException extends Exception {

    private final String option;

    /**
     * serialized version ID
     */
    private static final long serialVersionUID = 2731497960568930136L;

    public CashmallowException(String message) {
        super(message);
        this.option = null;
    }

    public CashmallowException(String message, Throwable e) {
        super(message, e);
        this.option = null;
    }

    public CashmallowException(String message, String option) {
        super(message);
        this.option = option;
    }

    public String getOption() {
        return option;
    }

}
