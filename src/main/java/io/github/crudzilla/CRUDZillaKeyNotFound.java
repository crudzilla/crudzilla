package io.github.crudzilla;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Nenhuma entidade encontrada para essa key")
public class CRUDZillaKeyNotFound extends RuntimeException {
}
