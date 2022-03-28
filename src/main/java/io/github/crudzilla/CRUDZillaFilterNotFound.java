package io.github.crudzilla;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Nenhum filtro encontrado para essa key/entidade")
public class CRUDZillaFilterNotFound extends RuntimeException {
}
