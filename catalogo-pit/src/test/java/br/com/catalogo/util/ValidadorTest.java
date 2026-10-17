package br.com.catalogo.util;

import br.com.catalogo.exception.ValidacaoException;
import br.com.catalogo.model.ItemMidia;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTest {

    private static final String OK_TITULO = "Dom Casmurro";
    private static final String OK_AUTOR = "Machado de Assis";
    private static final String OK_GENERO = "Romance";

    @Test
    void aceitaDadosValidosERemoveEspacos() throws Exception {
        ItemMidia item = Validador.validar("  " + OK_TITULO + " ", OK_AUTOR, " 1899 ",
                OK_GENERO, "Sinopse curta", "Livro");

        assertEquals(OK_TITULO, item.getTitulo());
        assertEquals(1899, item.getAnoLancamento());
        assertEquals("Livro", item.getTipoMidia());
    }

    @Test
    void sinopseVaziaViraNulaPorqueEhOpcional() throws Exception {
        ItemMidia item = Validador.validar(OK_TITULO, OK_AUTOR, "1899", OK_GENERO, "   ", "Livro");
        assertNull(item.getSinopse());
    }

    @Test
    void rejeitaAnoComLetras_casoDoEnunciado() {
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "abcd", OK_GENERO, "", "Livro"));
        assertTrue(e.getErros().get(0).contains("apenas números"));
    }

    @Test
    void rejeitaAnoDecimalOuComSimbolos() {
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "19.99", OK_GENERO, "", "Livro"));
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "1999; DROP TABLE item_midia", OK_GENERO, "", "Livro"));
    }

    @Test
    void rejeitaAnoMuitoGrandeParaCaberEmInt() {
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "99999999999999", OK_GENERO, "", "Livro"));
    }

    @Test
    void rejeitaAnoForaDoIntervalo() {
        int futuro = Year.now().getValue() + 2;
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "999", OK_GENERO, "", "Livro"));
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, String.valueOf(futuro), OK_GENERO, "", "Livro"));
    }

    @Test
    void rejeitaCamposObrigatoriosVaziosOuNulos() {
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> Validador.validar(null, "   ", "", null, null, null));
        // título, autor, gênero, tipo e ano
        assertEquals(5, e.getErros().size());
    }

    @Test
    void rejeitaTextosMaioresQueAsColunasDoBanco() {
        String longo = "a".repeat(Validador.MAX_TITULO + 1);
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> Validador.validar(longo, OK_AUTOR, "1999", OK_GENERO, "", "Livro"));
        assertTrue(e.getErros().get(0).contains("no máximo"));
    }

    @Test
    void rejeitaTipoDeMidiaInvalido() {
        assertThrows(ValidacaoException.class,
                () -> Validador.validar(OK_TITULO, OK_AUTOR, "1999", OK_GENERO, "", "Jogo"));
    }

    @Test
    void acumulaTodosOsErrosEmUmaSoExcecao() {
        ValidacaoException e = assertThrows(ValidacaoException.class,
                () -> Validador.validar("", OK_AUTOR, "xx", OK_GENERO, "", "Livro"));
        assertEquals(2, e.getErros().size());
    }
}
