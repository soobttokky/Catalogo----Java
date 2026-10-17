package br.com.catalogo.exception;

/**
 * Lançada quando ocorre uma falha de acesso ao banco de dados.
 * Encapsula a SQLException original (causa) e expõe uma mensagem
 * amigável, sem vazar detalhes internos (SQL, tabelas, servidor) ao usuário.
 */
public class PersistenciaException extends Exception {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
