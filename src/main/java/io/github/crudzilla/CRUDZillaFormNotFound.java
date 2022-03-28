package io.github.crudzilla;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Nenhum Form encontrado para essa entidade")
public class CRUDZillaFormNotFound extends RuntimeException {
}
