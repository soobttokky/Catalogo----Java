# Catálogo Simples de Livros, Séries e Filmes — Situação-Problema 2

Tratamento de exceções e validação de dados no cadastro de itens
(Java 17, Servlets, JSP/JSTL, JDBC, MySQL, JUnit 5).

## Estrutura

```
sql/schema.sql                      script de criação do banco
src/main/java/.../model             ItemMidia (entidade)
src/main/java/.../exception         ValidacaoException, PersistenciaException
src/main/java/.../util              Validador (regras de validação)
src/main/java/.../dao               FabricaDeConexoes, ItemMidiaDAO (JDBC + PreparedStatement)
src/main/java/.../servlet           CadastrarItemServlet, ListarItensServlet
src/main/webapp                     JSPs (cadastroItem, listagem, erro), CSS e web.xml
src/test/java/.../ValidadorTest     testes unitários (JUnit 5)
```

## Como executar

1. Instale JDK 17+, Maven 3.9+, MySQL 8+ e Apache Tomcat 10.1+ (Jakarta EE).
2. Crie o banco: `mysql -u root -p < sql/schema.sql`
3. Ajuste usuário/senha em `src/main/resources/db.properties`.
4. Rode os testes: `mvn test`
5. Gere o pacote: `mvn clean package` (arquivo `target/catalogo.war`).
6. Copie `catalogo.war` para a pasta `webapps` do Tomcat e acesse
   `http://localhost:8080/catalogo/cadastrar`.

## Como testar o tratamento de erros

| Cenário | Resultado esperado |
|---|---|
| Cadastro válido | Redireciona para `/listar` com mensagem de sucesso |
| Ano com letras (ex.: `abcd`) | Formulário reaparece com a mensagem "O ano deve conter apenas números", mantendo o que foi digitado |
| Campos obrigatórios vazios | Todas as mensagens de erro de uma só vez |
| Título com mais de 255 caracteres | Mensagem de limite de tamanho |
| Banco desligado | Página `erro.jsp` com mensagem amigável (sem stack trace); detalhe no log |
