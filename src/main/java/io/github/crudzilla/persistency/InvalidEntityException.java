package io.github.crudzilla.persistency;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Dados inválidos")
public class InvalidEntityException extends RuntimeException {

	private static final long serialVersionUID = 2L;
	
	private final List<String> mensagens;

    public <E extends CRUDZillaEntity> InvalidEntityException(Set<ConstraintViolation<E>> violations) {
        this.mensagens = new ArrayList<>();
        for (ConstraintViolation<E> violation : violations) {
            mensagens.add(violation.getMessage());
        }
    }

    /**
     * Cria exceção apontando uma lista de mensagens de erro e uma causa raiz.
     * @param mensagens Lista de mensagens detalhando porque a entidade é inválida.
     * @param causa Causa da exceção atual.
     */
    public InvalidEntityException(List<String> mensagens, Throwable causa) {
        super(mensagens.get(0), causa);
    	this.mensagens = mensagens;
    }

    /**
     * Cria exceção apontando uma lista de mensagens de erro.
     * @param mensagens Lista de mensagens detalhando porque a entidade é inválida.
     */
    public InvalidEntityException(List<String> mensagens) {
        super(mensagens.get(0));
    	this.mensagens = mensagens;
    }

    /**
     * Cria exceção apontando uma mensagen de erro e uma causa raiz.
     * @param mensagem Mensagem detalhando porque a entidade é inválida.
     * @param causa Causa da exceção atual.
     */
    public InvalidEntityException(String mensagem, Throwable causa) {
    	this(Arrays.asList(mensagem), causa);
    }

    /**
     * Cria exceção apontando uma mensagen de erro.
     * @param mensagem Mensagem detalhando porque a entidade é inválida.
     */
    public InvalidEntityException(String mensagem) {
    	this(Arrays.asList(mensagem));
    }
    
    public List<String> getMensagens() {
        return mensagens;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        message.append("\nErros encontrados na validação da entidade: ");
        for(String s : mensagens) {
            message.append(s).append("  \n");
        }
        message.append("\n");
        return message.toString();
    }

}