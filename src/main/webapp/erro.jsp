<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Ops! Algo deu errado</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/estilo.css">
</head>
<body>
<header>
    <a href="${pageContext.request.contextPath}/cadastrar">Cadastrar</a>
    <a href="${pageContext.request.contextPath}/listar">Catálogo</a>
</header>
<main>
    <h1>Ops! Algo deu errado</h1>
    <div class="erros" role="alert">
        <c:choose>
            <c:when test="${not empty mensagemErro}"><c:out value="${mensagemErro}"/></c:when>
            <c:otherwise>Não foi possível concluir a operação. Tente novamente em instantes.</c:otherwise>
        </c:choose>
    </div>
    <%-- Nenhum stack trace é exibido: o detalhe técnico fica apenas no log do servidor --%>
    <p><a href="${pageContext.request.contextPath}/cadastrar">Voltar ao cadastro</a></p>
</main>
</body>
</html>
