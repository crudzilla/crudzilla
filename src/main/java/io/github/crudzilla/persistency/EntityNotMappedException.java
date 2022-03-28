package io.github.crudzilla.persistency;

public class EntityNotMappedException extends RuntimeException {
    public EntityNotMappedException(String s, Throwable e) {
        super(s, e);
    }
}
