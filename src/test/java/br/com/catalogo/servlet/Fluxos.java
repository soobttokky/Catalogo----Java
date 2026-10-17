package br.com.catalogo.servlet;

import br.com.catalogo.dao.ItemMidiaDAO;
import br.com.catalogo.exception.PersistenciaException;
import br.com.catalogo.model.ItemMidia;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/** Harness: executa os fluxos reais (Servlets, DAO, Validador) com banco e container simulados. */
public class Fluxos {
    // ---- estado do banco falso
    static int conexoesAbertas, conexoesFechadas, stmtAbertos, stmtFechados;
    static String modo = "ok"; // ok | falhaInsert | semConexao | erroInesperado
    static String ultimoSql; static List<Object> params = new ArrayList<>();

    static class FakeDriver implements Driver {
        public boolean acceptsURL(String u){ return u.startsWith("jdbc:mysql"); }
        public Connection connect(String u, Properties p) throws SQLException {
            if (!acceptsURL(u)) return null;
            if (modo.equals("semConexao")) throw new SQLException("Communications link failure", "08S01", 0);
            if (modo.equals("erroInesperado")) throw new IllegalStateException("falha inesperada simulada");
            conexoesAbertas++;
            return (Connection) Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{Connection.class}, (px, m, a) -> {
                switch (m.getName()) {
                    case "prepareStatement": ultimoSql = (String) a[0]; params.clear(); stmtAbertos++; return stmt();
                    case "close": conexoesFechadas++; return null;
                    default: return null; } });
        }
        public int getMajorVersion(){return 1;} public int getMinorVersion(){return 0;} public boolean jdbcCompliant(){return false;}
        public java.util.logging.Logger getParentLogger(){return null;}
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p){return new DriverPropertyInfo[0];}
    }
    static PreparedStatement stmt() {
        return (PreparedStatement) Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, (px, m, a) -> {
            String n = m.getName();
            if (n.startsWith("set")) { params.add(a[1]); return null; }
            if (n.equals("executeUpdate")) { if (modo.equals("falhaInsert")) throw new SQLException("Duplicate/constraint violation", "23000", 3819); return 1; }
            if (n.equals("getGeneratedKeys")) return rs(List.of(Map.of("1", 42)));
            if (n.equals("executeQuery")) return rs(List.of(
                Map.of("id",1,"titulo","Dom Casmurro","autor_diretor","Machado de Assis","ano_lancamento",1899,"genero","Romance","sinopse","x","tipo_midia","Livro"),
                Map.of("id",2,"titulo","Matrix","autor_diretor","Wachowski","ano_lancamento",1999,"genero","Ficção","sinopse","y","tipo_midia","Filme")));
            if (n.equals("close")) { stmtFechados++; return null; }
            return null; });
    }
    static ResultSet rs(List<Map<String,Object>> rows) {
        int[] i = {-1};
        return (ResultSet) Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{ResultSet.class}, (px, m, a) -> {
            switch (m.getName()) {
                case "next": return ++i[0] < rows.size();
                case "getInt": return a[0] instanceof Integer ? 42 : (Integer) rows.get(i[0]).get(a[0]);
                case "getString": return (String) rows.get(i[0]).get(a[0]);
                default: return null; } });
    }

    // ---- container falso
    static class Ctx { Map<String,String> p = new HashMap<>(); Map<String,Object> attr = new LinkedHashMap<>();
        String forward, redirect; Integer status; }
    static Ctx ctx;
    static HttpServletRequest req(Ctx c) {
        return (HttpServletRequest) Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (px, m, a) -> {
            switch (m.getName()) {
                case "getParameter": return c.p.get(a[0]);
                case "setAttribute": c.attr.put((String) a[0], a[1]); return null;
                case "getContextPath": return "/catalogo";
                case "getRequestDispatcher": return Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{RequestDispatcher.class},
                        (p2, m2, a2) -> { c.forward = (String) a[0]; return null; });
                default: return null; } });
    }
    static HttpServletResponse resp(Ctx c) {
        return (HttpServletResponse) Proxy.newProxyInstance(Fluxos.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class}, (px, m, a) -> {
            if (m.getName().equals("sendRedirect")) c.redirect = (String) a[0];
            if (m.getName().equals("setStatus")) c.status = (Integer) a[0];
            return null; });
    }
    static Ctx form(String t, String a, String ano, String g, String s, String tipo) {
        Ctx c = new Ctx(); c.p.put("titulo", t); c.p.put("autorDiretor", a); c.p.put("ano", ano); c.p.put("genero", g); c.p.put("sinopse", s); c.p.put("tipoMidia", tipo); return c; }
    static void reset(String m) { modo = m; conexoesAbertas = conexoesFechadas = stmtAbertos = stmtFechados = 0; ultimoSql = null; params.clear(); }

    static int ok = 0, fail = 0;
    static void check(String desc, boolean cond) { if (cond) ok++; else fail++; System.out.println("   [" + (cond ? "OK" : "FALHOU") + "] " + desc); }
    static void titulo(String t) { System.out.println("\n" + t); }

    public static void main(String[] args) throws Exception {
        DriverManager.registerDriver(new FakeDriver());
        java.util.logging.Logger.getLogger("br.com.catalogo").setLevel(java.util.logging.Level.OFF); // silencia log no resumo
        CadastrarItemServlet cad = new CadastrarItemServlet(); ListarItensServlet lis = new ListarItensServlet();

        titulo("Fluxo 1 - DAO.inserir com sucesso (try + finally)");
        reset("ok"); ItemMidia it = new ItemMidia(); it.setTitulo("Dom Casmurro"); it.setAutorDiretor("Machado de Assis");
        it.setAnoLancamento(1899); it.setGenero("Romance"); it.setTipoMidia("Livro");
        new ItemMidiaDAO().inserir(it);
        check("id gerado atribuído ao item (42)", it.getId() != null && it.getId() == 42);
        check("conexão e statement fechados pelo finally", conexoesAbertas == 1 && conexoesFechadas == 1 && stmtAbertos == 1 && stmtFechados == 1);
        check("SQL usa 6 parâmetros '?'", ultimoSql.chars().filter(ch -> ch == '?').count() == 6);

        titulo("Fluxo 2 - DAO.inserir com falha no banco (catch + finally)");
        reset("falhaInsert");
        try { new ItemMidiaDAO().inserir(it); check("deveria lançar exceção", false); }
        catch (PersistenciaException e) {
            check("SQLException convertida em PersistenciaException amigável", e.getMessage().startsWith("Não foi possível salvar"));
            check("mensagem não vaza detalhes técnicos (SQL/constraint)", !e.getMessage().contains("constraint") && !e.getMessage().contains("INSERT"));
            check("causa original preservada (SQLException)", e.getCause() instanceof SQLException);
        }
        check("finally fechou statement e conexão mesmo com erro", conexoesFechadas == 1 && stmtFechados == 1);

        titulo("Fluxo 3 - Cadastro válido pelo Servlet (POST)");
        reset("ok"); ctx = form("  Matrix ", "Wachowski", "1999", "Ficção", "", "Filme");
        cad.doPost(req(ctx), resp(ctx));
        check("redireciona para /catalogo/listar?sucesso=1", "/catalogo/listar?sucesso=1".equals(ctx.redirect));
        check("não define status de erro", ctx.status == null);
        check("valores normalizados (trim) enviados ao banco: 'Matrix'", params.get(0).equals("Matrix"));
        check("ano enviado como inteiro 1999", params.get(2).equals(1999));
        check("sinopse vazia gravada como NULL", params.get(4) == null);

        titulo("Fluxo 4 - Caso do enunciado: ano com letras");
        reset("ok"); ctx = form("Dom Casmurro", "Machado de Assis", "abcd", "Romance", "", "Livro");
        cad.doPost(req(ctx), resp(ctx));
        check("HTTP 400 e volta ao formulário (/cadastroItem.jsp)", ctx.status == 400 && "/cadastroItem.jsp".equals(ctx.forward));
        check("mensagem: 'O ano deve conter apenas números'", ((List<?>) ctx.attr.get("erros")).get(0).toString().contains("apenas números"));
        check("dados digitados devolvidos ao formulário", "Dom Casmurro".equals(ctx.attr.get("titulo")) && "abcd".equals(ctx.attr.get("ano")));
        check("banco NÃO foi acessado (nenhuma conexão aberta)", conexoesAbertas == 0);

        titulo("Fluxo 5 - Vários campos vazios/nulos");
        reset("ok"); ctx = form("", "   ", "", null, null, null);
        cad.doPost(req(ctx), resp(ctx));
        check("5 erros reportados de uma vez", ((List<?>) ctx.attr.get("erros")).size() == 5);
        check("HTTP 400 e nenhuma conexão aberta", ctx.status == 400 && conexoesAbertas == 0);

        titulo("Fluxo 6 - Ano fora do intervalo e título longo demais");
        reset("ok"); ctx = form("a".repeat(256), "Autor", "999", "Drama", "", "Livro");
        cad.doPost(req(ctx), resp(ctx));
        check("2 erros (tamanho do título e intervalo do ano)", ((List<?>) ctx.attr.get("erros")).size() == 2);

        titulo("Fluxo 7 - Tentativa de SQL Injection no título");
        reset("ok"); String ataque = "x'); DROP TABLE item_midia;--";
        ctx = form(ataque, "Autor", "2000", "Drama", "", "Livro");
        cad.doPost(req(ctx), resp(ctx));
        check("cadastro segue o fluxo normal (texto é só dado)", "/catalogo/listar?sucesso=1".equals(ctx.redirect));
        check("o ataque foi enviado como parâmetro, não concatenado ao SQL", params.get(0).equals(ataque) && !ultimoSql.contains("DROP"));

        titulo("Fluxo 8 - Banco indisponível");
        reset("semConexao"); ctx = form("Matrix", "Wachowski", "1999", "Ficção", "", "Filme");
        cad.doPost(req(ctx), resp(ctx));
        check("HTTP 500 e página /erro.jsp", ctx.status == 500 && "/erro.jsp".equals(ctx.forward));
        check("mensagem amigável ao usuário", String.valueOf(ctx.attr.get("mensagemErro")).startsWith("Não foi possível salvar"));
        check("nenhum detalhe técnico no request (sem 'Communications')", !String.valueOf(ctx.attr.values()).contains("Communications"));

        titulo("Fluxo 9 - Erro inesperado (RuntimeException)");
        reset("erroInesperado"); ctx = form("Matrix", "Wachowski", "1999", "Ficção", "", "Filme");
        cad.doPost(req(ctx), resp(ctx));
        check("rede de segurança: HTTP 500 e /erro.jsp", ctx.status == 500 && "/erro.jsp".equals(ctx.forward));
        check("mensagem genérica, sem stack trace", "Ocorreu um erro inesperado. Tente novamente.".equals(ctx.attr.get("mensagemErro")));

        titulo("Fluxo 10 - GET do formulário");
        reset("ok"); ctx = new Ctx(); cad.doGet(req(ctx), resp(ctx));
        check("encaminha para /cadastroItem.jsp", "/cadastroItem.jsp".equals(ctx.forward));

        titulo("Fluxo 11 - Listagem (sucesso e falha)");
        reset("ok"); ctx = new Ctx(); lis.doGet(req(ctx), resp(ctx));
        check("2 itens carregados e /listagem.jsp", ((List<?>) ctx.attr.get("itens")).size() == 2 && "/listagem.jsp".equals(ctx.forward));
        check("recursos fechados (try-with-resources)", conexoesFechadas == 1 && stmtFechados == 1);
        reset("semConexao"); ctx = new Ctx(); lis.doGet(req(ctx), resp(ctx));
        check("banco fora: HTTP 500 e /erro.jsp", ctx.status == 500 && "/erro.jsp".equals(ctx.forward));

        System.out.println("\nTOTAL: " + (ok + fail) + " verificações | OK: " + ok + " | FALHOU: " + fail);
        System.exit(fail == 0 ? 0 : 1);
    }
}
