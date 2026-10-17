<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Cadastrar item - Catálogo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/estilo.css">
</head>
<body>
<header>
    <a href="${pageContext.request.contextPath}/cadastrar">Cadastrar</a>
    <a href="${pageContext.request.contextPath}/listar">Catálogo</a>
</header>
<main>
    <h1>Cadastrar novo item</h1>

    <%-- Mensagens de validação enviadas pelo Servlet --%>
    <c:if test="${not empty erros}">
        <div class="erros" role="alert">
            <strong>Corrija os campos abaixo:</strong>
            <ul>
                <c:forEach var="erro" items="${erros}">
                    <%-- c:out escapa HTML/JS (proteção contra XSS) --%>
                    <li><c:out value="${erro}"/></li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <%--
        Validação do lado do cliente (required, maxlength, min/max) melhora a experiência,
        mas NÃO substitui a validação do servidor: ela pode ser burlada.
    --%>
    <form method="post" action="${pageContext.request.contextPath}/cadastrar">
        <label for="titulo">Título *</label>
        <input type="text" id="titulo" name="titulo" maxlength="255" required
               value="<c:out value='${titulo}'/>">

        <label for="autorDiretor">Autor/Diretor *</label>
        <input type="text" id="autorDiretor" name="autorDiretor" maxlength="255" required
               value="<c:out value='${autorDiretor}'/>">

        <label for="ano">Ano de lançamento *</label>
        <input type="number" id="ano" name="ano" min="1000" max="9999" required
               value="<c:out value='${ano}'/>">

        <label for="genero">Gênero *</label>
        <input type="text" id="genero" name="genero" maxlength="100" required
               value="<c:out value='${genero}'/>">

        <label for="tipoMidia">Tipo *</label>
        <select id="tipoMidia" name="tipoMidia" required>
            <option value="Livro"  ${tipoMidia == 'Livro'  ? 'selected' : ''}>Livro</option>
            <option value="Filme"  ${tipoMidia == 'Filme'  ? 'selected' : ''}>Filme</option>
            <option value="Série"  ${tipoMidia == 'Série'  ? 'selected' : ''}>Série</option>
        </select>

        <label for="sinopse">Sinopse</label>
        <textarea id="sinopse" name="sinopse" rows="5" maxlength="2000"><c:out value="${sinopse}"/></textarea>

        <button type="submit">Salvar</button>
    </form>
</main>
</body>
</html>
