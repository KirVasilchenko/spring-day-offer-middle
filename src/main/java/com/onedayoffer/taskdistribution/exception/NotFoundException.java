package com.onedayoffer.taskdistribution.exception;

public abstract class NotFoundException extends RuntimeException {

    NotFoundException(String message) {
        super(message);
    }
}
