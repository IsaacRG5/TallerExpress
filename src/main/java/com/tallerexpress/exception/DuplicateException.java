package com.tallerexpress.exception;

public class DuplicateException extends BusinessException {
    private static final long serialVersionUID = 1L;
    public DuplicateException(String message) { super(message); }
}
