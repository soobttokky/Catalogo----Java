package br.com.catalogo.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lançada quando os dados enviados pelo usuário não passam na validação.
 * Carrega TODAS as mensagens de erro, para que o usuário corrija o
 * formulário de uma só vez (e não um erro por tentativa).
 */
public class ValidacaoException extends Exception {

    private static final long serialVersionUID = 1L;

    // ArrayList (Serializable) em vez de List, pois a exceção é serializável
    private final ArrayList<String> erros;

    public ValidacaoException(List<String> erros) {
        super("Dados inválidos: " + String.join("; ", erros));
        this.erros = new ArrayList<>(erros);
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }
}
