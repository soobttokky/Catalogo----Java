# Catálogo Simples de Livros, Séries e Filmes — Situação-Problema 2

Tratamento de exceções e validação de dados no cadastro de itens
(Java 17, Servlets, JSP/JSTL, JDBC, MySQL, JUnit 5).

## Estrutura

```
resultado_fluxos.txt                saída de referência do Fluxos (30 verificações)
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
2. Crie o banco: `mysql -u root -p --default-character-set=utf8mb4 < sql/schema.sql`
3. Ajuste usuário/senha em `src/main/resources/db.properties`. A URL já traz `allowPublicKeyRetrieval=true`
   (necessário no MySQL 8 com `useSSL=false`); isso vale só para desenvolvimento local.
4. Rode os testes: `mvn test`
5. Gere o pacote: `mvn clean package` (arquivo `target/catalogo.war`).
6. Copie `catalogo.war` para a pasta `webapps` do Tomcat e acesse
   `http://localhost:8080/catalogo/cadastrar`.

## Como testar o tratamento de erros

O campo **Ano** do formulário é `type="number"`: o navegador já impede letras (primeira camada de defesa),
então o texto `abcd` só chega ao servidor se a validação do navegador for contornada. Há duas formas:

- **curl** (envia direto ao Servlet, sem passar pelo navegador):
  ```
  curl -i --data-urlencode "titulo=Dom Casmurro" --data-urlencode "autorDiretor=Machado de Assis" \
       --data-urlencode "ano=abcd" --data-urlencode "genero=Romance" --data-urlencode "tipoMidia=Livro" \
       http://localhost:8080/catalogo/cadastrar
  ```
  A resposta é `HTTP/1.1 400` com o formulário e a mensagem de erro.
- **Navegador**: nas ferramentas do desenvolvedor (F12), troque `type="number"` por `type="text"` no campo
  Ano, digite `abcd` e envie.

Sem contornar nada, o ano `2099` também mostra a validação do servidor: o navegador aceita (o limite do HTML é 9999),
mas o servidor só aceita até o ano atual + 1.

| Cenário | Resultado esperado |
|---|---|
| Cadastro válido | Redireciona para `/listar` com mensagem de sucesso |
| Ano com letras (ex.: `abcd`, via curl ou DevTools) | HTTP 400; o formulário reaparece com a mensagem "O ano deve conter apenas números (exemplo: 1999).", mantendo o que foi digitado |
| Ano `2099` (aceito pelo navegador) | Formulário reaparece com "O ano deve estar entre 1000 e (ano atual + 1)." |
| Campos obrigatórios vazios (ex.: título só com espaços) | Todas as mensagens de erro de uma só vez |
| Título com mais de 255 caracteres | Mensagem de limite de tamanho |
| Banco desligado | Página `erro.jsp` com mensagem amigável (sem stack trace); detalhe no log |

## Verificação dos fluxos (sem Tomcat e sem MySQL)

`src/test/java/.../servlet/Fluxos.java` executa os Servlets, o DAO e o Validador reais usando um
driver JDBC falso e requisição/resposta simuladas (qualquer driver real no classpath é desregistrado,
então nenhum banco de verdade é tocado). Rode o método `main` pela IDE (ou
`mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=br.com.catalogo.servlet.Fluxos`).
A saída de referência está em `resultado_fluxos.txt`.
Cobre 11 fluxos (30 verificações): cadastro válido, ano com letras, campos vazios, limites,
SQL Injection, banco indisponível, erro inesperado, formulário e listagem.
