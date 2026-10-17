package br.com.catalogo.util;

import br.com.catalogo.exception.ValidacaoException;
import br.com.catalogo.model.ItemMidia;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Valida os dados do formulário de cadastro ANTES de qualquer acesso ao banco.
 *
 * Regras:
 *  - campos obrigatórios não podem estar vazios;
 *  - o ano deve ser um número inteiro dentro de um intervalo plausível;
 *  - os textos não podem ultrapassar o tamanho das colunas do banco;
 *  - o tipo de mídia deve ser um dos valores permitidos.
 */
public final class Validador {

    public static final int MAX_TITULO = 255;
    public static final int MAX_AUTOR_DIRETOR = 255;
    public static final int MAX_GENERO = 100;
    public static final int MAX_SINOPSE = 2000;
    public static final int ANO_MINIMO = 1000;
    public static final Set<String> TIPOS_PERMITIDOS = Set.of("Livro", "Filme", "Série");

    private Validador() {
    }

    /**
     * Valida os campos recebidos (sempre como texto, como chegam do formulário)
     * e devolve o objeto {@link ItemMidia} pronto para ser persistido.
     *
     * @throws ValidacaoException com a lista completa de problemas encontrados
     */
    public static ItemMidia validar(String titulo, String autorDiretor, String ano,
                                    String genero, String sinopse, String tipoMidia)
            throws ValidacaoException {

        List<String> erros = new ArrayList<>();

        titulo = normalizar(titulo);
        autorDiretor = normalizar(autorDiretor);
        ano = normalizar(ano);
        genero = normalizar(genero);
        sinopse = normalizar(sinopse);
        tipoMidia = normalizar(tipoMidia);

        validarTextoObrigatorio("Título", titulo, MAX_TITULO, erros);
        validarTextoObrigatorio("Autor/Diretor", autorDiretor, MAX_AUTOR_DIRETOR, erros);
        validarTextoObrigatorio("Gênero", genero, MAX_GENERO, erros);

        if (sinopse.length() > MAX_SINOPSE) {
            erros.add("A sinopse deve ter no máximo " + MAX_SINOPSE + " caracteres.");
        }

        if (!TIPOS_PERMITIDOS.contains(tipoMidia)) {
            erros.add("Tipo de mídia inválido. Escolha Livro, Filme ou Série.");
        }

        Integer anoLancamento = validarAno(ano, erros);

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        ItemMidia item = new ItemMidia();
        item.setTitulo(titulo);
        item.setAutorDiretor(autorDiretor);
        item.setAnoLancamento(anoLancamento);
        item.setGenero(genero);
        item.setSinopse(sinopse.isEmpty() ? null : sinopse);
        item.setTipoMidia(tipoMidia);
        return item;
    }

    private static void validarTextoObrigatorio(String campo, String valor, int max, List<String> erros) {
        if (valor.isEmpty()) {
            erros.add("O campo " + campo + " é obrigatório.");
        } else if (valor.length() > max) {
            erros.add("O campo " + campo + " deve ter no máximo " + max + " caracteres.");
        }
    }

    /**
     * Converte o ano de texto para número. É aqui que o erro do enunciado
     * ("o campo ano pode receber strings") é tratado: a NumberFormatException
     * é capturada e vira uma mensagem clara, em vez de derrubar a aplicação.
     */
    private static Integer validarAno(String ano, List<String> erros) {
        if (ano.isEmpty()) {
            erros.add("O campo Ano é obrigatório.");
            return null;
        }
        int anoMaximo = Year.now().getValue() + 1; // permite lançamentos do próximo ano
        try {
            int valor = Integer.parseInt(ano);
            if (valor < ANO_MINIMO || valor > anoMaximo) {
                erros.add("O ano deve estar entre " + ANO_MINIMO + " e " + anoMaximo + ".");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            erros.add("O ano deve conter apenas números (exemplo: 1999).");
            return null;
        }
    }

    /** Evita NullPointerException e remove espaços nas pontas. */
    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
