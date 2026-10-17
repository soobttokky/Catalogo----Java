package br.com.catalogo.servlet;

import br.com.catalogo.dao.ItemMidiaDAO;
import br.com.catalogo.exception.PersistenciaException;
import br.com.catalogo.exception.ValidacaoException;
import br.com.catalogo.model.ItemMidia;
import br.com.catalogo.util.Validador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador do cadastro. Fluxo:
 *  1) lê os parâmetros do formulário (sempre texto);
 *  2) valida (Validador) -> ValidacaoException se houver erro;
 *  3) persiste (DAO)     -> PersistenciaException se o banco falhar;
 *  4) redireciona para a listagem (padrão Post/Redirect/Get).
 * Cada tipo de falha tem seu catch, com resposta adequada ao usuário.
 */
@WebServlet("/cadastrar")
public class CadastrarItemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CadastrarItemServlet.class.getName());

    private final ItemMidiaDAO dao = new ItemMidiaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/cadastroItem.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String titulo = request.getParameter("titulo");
        String autorDiretor = request.getParameter("autorDiretor");
        String ano = request.getParameter("ano");
        String genero = request.getParameter("genero");
        String sinopse = request.getParameter("sinopse");
        String tipoMidia = request.getParameter("tipoMidia");

        try {
            ItemMidia item = Validador.validar(titulo, autorDiretor, ano, genero, sinopse, tipoMidia);
            dao.inserir(item);
            response.sendRedirect(request.getContextPath() + "/listar?sucesso=1");

        } catch (ValidacaoException e) {
            // Erro do usuário: devolve o formulário com as mensagens e os dados já digitados
            request.setAttribute("erros", e.getErros());
            request.setAttribute("titulo", titulo);
            request.setAttribute("autorDiretor", autorDiretor);
            request.setAttribute("ano", ano);
            request.setAttribute("genero", genero);
            request.setAttribute("sinopse", sinopse);
            request.setAttribute("tipoMidia", tipoMidia);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.getRequestDispatcher("/cadastroItem.jsp").forward(request, response);

        } catch (PersistenciaException e) {
            // Erro de infraestrutura: mensagem amigável (o detalhe técnico já foi para o log)
            request.setAttribute("mensagemErro", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/erro.jsp").forward(request, response);

        } catch (RuntimeException e) {
            // Rede de segurança para falhas inesperadas
            LOGGER.log(Level.SEVERE, "Erro inesperado no cadastro", e);
            request.setAttribute("mensagemErro", "Ocorreu um erro inesperado. Tente novamente.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/erro.jsp").forward(request, response);
        }
    }
}
