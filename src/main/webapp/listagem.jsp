<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Catálogo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/estilo.css">
</head>
<body>
<header>
    <a href="${pageContext.request.contextPath}/cadastrar">Cadastrar</a>
    <a href="${pageContext.request.contextPath}/listar">Catálogo</a>
</header>
<main>
    <h1>Catálogo</h1>

    <c:if test="${param.sucesso == '1'}">
        <div class="sucesso" role="status">Item cadastrado com sucesso!</div>
    </c:if>

    <c:choose>
        <c:when test="${empty itens}">
            <p>Nenhum item cadastrado ainda.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr><th>Título</th><th>Autor/Diretor</th><th>Ano</th><th>Gênero</th><th>Tipo</th></tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${itens}">
                    <tr>
                        <td><c:out value="${item.titulo}"/></td>
                        <td><c:out value="${item.autorDiretor}"/></td>
                        <td><c:out value="${item.anoLancamento}"/></td>
                        <td><c:out value="${item.genero}"/></td>
                        <td><c:out value="${item.tipoMidia}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
