package io.github.crudzilla;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Método no User Service Inválido.")
public class CRUDZillaInvalidOperation extends RuntimeException {
}
