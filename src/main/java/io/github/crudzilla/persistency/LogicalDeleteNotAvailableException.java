package io.github.crudzilla.persistency;

public class LogicalDeleteNotAvailableException extends RuntimeException {
    public LogicalDeleteNotAvailableException() {
        super("Essa entidade não suporta ativação/inativação");
    }
}
