package com.g4.api.model;

public enum TaskStatus {
    BACKLOG(1),
    TODO(2),
    IN_PROGRESS(3),
    DONE(4),
    CLOSED(5);

    private final int value;

    TaskStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskStatus fromValue(int value) {
        for (TaskStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return BACKLOG;
    }
}
