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
 *
 * O arquivo é carregado sob demanda (na primeira conexão) e não em um bloco
 * static: se a configuração estiver ausente ou ilegível, o erro sai como
 * IllegalStateException (uma RuntimeException, capturada pela rede de segurança
 * do Servlet). Um static que lançasse exceção viraria ExceptionInInitializerError
 * e, nas chamadas seguintes, NoClassDefFoundError, que não são RuntimeException.
 */
public final class FabricaDeConexoes {

    private static Properties config;

    private FabricaDeConexoes() {
    }

    public static Connection getConexao() throws SQLException {
        Properties cfg = configuracao();
        return DriverManager.getConnection(
                cfg.getProperty("db.url"),
                cfg.getProperty("db.user"),
                cfg.getProperty("db.password"));
    }

    private static synchronized Properties configuracao() {
        if (config == null) {
            Properties lida = new Properties();
            try (InputStream in = FabricaDeConexoes.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException("Arquivo db.properties não encontrado no classpath.");
                }
                lida.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Falha ao ler db.properties.", e);
            }
            config = lida;
        }
        return config;
    }
}
