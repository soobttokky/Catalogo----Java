package br.com.catalogo.dao;

import br.com.catalogo.exception.PersistenciaException;
import br.com.catalogo.model.ItemMidia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object): único ponto de acesso à tabela item_midia.
 * Toda SQLException é registrada em log e convertida em
 * {@link PersistenciaException}, com mensagem amigável.
 */
public class ItemMidiaDAO {

    private static final Logger LOGGER = Logger.getLogger(ItemMidiaDAO.class.getName());

    private static final String SQL_INSERIR =
            "INSERT INTO item_midia (titulo, autor_diretor, ano_lancamento, genero, sinopse, tipo_midia) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR =
            "SELECT id, titulo, autor_diretor, ano_lancamento, genero, sinopse, tipo_midia "
                    + "FROM item_midia ORDER BY titulo";

    /**
     * Insere um item usando try-catch-finally explícito:
     *  - try: executa o INSERT;
     *  - catch: registra o erro técnico e lança uma exceção amigável;
     *  - finally: SEMPRE libera statement e conexão, com ou sem erro.
     */
    public void inserir(ItemMidia item) throws PersistenciaException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = FabricaDeConexoes.getConexao();
            stmt = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, item.getTitulo());
            stmt.setString(2, item.getAutorDiretor());
            stmt.setInt(3, item.getAnoLancamento());
            stmt.setString(4, item.getGenero());
            stmt.setString(5, item.getSinopse());
            stmt.setString(6, item.getTipoMidia());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    item.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inserir item no catálogo (SQLState="
                    + e.getSQLState() + ", código=" + e.getErrorCode() + ")", e);
            throw new PersistenciaException(
                    "Não foi possível salvar o item no momento. Tente novamente em instantes.", e);
        } finally {
            fechar(stmt);
            fechar(conn);
        }
    }

    /**
     * Lista os itens do catálogo. Aqui é usado try-with-resources, a forma
     * moderna do "finally": os recursos são fechados automaticamente.
     */
    public List<ItemMidia> listar() throws PersistenciaException {
        List<ItemMidia> itens = new ArrayList<>();
        try (Connection conn = FabricaDeConexoes.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ItemMidia item = new ItemMidia();
                item.setId(rs.getInt("id"));
                item.setTitulo(rs.getString("titulo"));
                item.setAutorDiretor(rs.getString("autor_diretor"));
                item.setAnoLancamento(rs.getInt("ano_lancamento"));
                item.setGenero(rs.getString("genero"));
                item.setSinopse(rs.getString("sinopse"));
                item.setTipoMidia(rs.getString("tipo_midia"));
                itens.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar itens do catálogo", e);
            throw new PersistenciaException("Não foi possível carregar o catálogo no momento.", e);
        }
        return itens;
    }

    /** Fecha um recurso sem deixar uma falha de fechamento mascarar o erro original. */
    private void fechar(AutoCloseable recurso) {
        if (recurso != null) {
            try {
                recurso.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Falha ao fechar recurso JDBC", e);
            }
        }
    }
}
