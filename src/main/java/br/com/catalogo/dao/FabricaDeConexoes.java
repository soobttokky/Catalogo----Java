package br.com.catalogo.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centraliza a criação de conexões JDBC. Lê url, usuário e senha do
 * arquivo db.properties, evitando credenciais fixas no código.
 */
public final class FabricaDeConexoes {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = FabricaDeConexoes.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Arquivo db.properties não encontrado no classpath.");
            }
            CONFIG.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler db.properties.", e);
        }
    }

    private FabricaDeConexoes() {
    }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }
}
