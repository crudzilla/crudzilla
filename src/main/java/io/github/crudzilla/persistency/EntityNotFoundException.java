package io.github.crudzilla.persistency;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Entidade não encontrada.")
public class EntityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public <E extends CRUDZillaEntity, K> EntityNotFoundException(Class<E> classe, K idEntidade) {
        super("A entidade '" + classe.getSimpleName() + "' com o id '" + idEntidade.toString() + "' não foi encontrada.");
    }

    public <E extends CRUDZillaEntity> EntityNotFoundException(Class<E> classe) {
        super("Nenhuma entidade '" + classe.getSimpleName() + "' que atendesse aos critérios desejados foi encontrada.");
    }

    public <E extends CRUDZillaEntity> EntityNotFoundException(Class<E> classe, String criterios) {
        super("Nenhuma entidade '" + classe.getSimpleName() + "' que atendesse aos critérios informados foi encontrada. Critérios: " + criterios);
    }
}