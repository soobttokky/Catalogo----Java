package br.com.catalogo.servlet;

import br.com.catalogo.dao.ItemMidiaDAO;
import br.com.catalogo.exception.PersistenciaException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Exibe a listagem de itens (usada para confirmar visualmente o cadastro). */
@WebServlet("/listar")
public class ListarItensServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ItemMidiaDAO dao = new ItemMidiaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("itens", dao.listar());
            request.getRequestDispatcher("/listagem.jsp").forward(request, response);
        } catch (PersistenciaException e) {
            request.setAttribute("mensagemErro", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.getRequestDispatcher("/erro.jsp").forward(request, response);
        }
    }
}
