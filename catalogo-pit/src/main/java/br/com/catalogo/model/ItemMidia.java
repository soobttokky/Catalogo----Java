package br.com.catalogo.model;

/**
 * Entidade principal do catálogo: representa um livro, filme ou série.
 */
public class ItemMidia {

    private Integer id;
    private String titulo;
    private String autorDiretor;
    private Integer anoLancamento;
    private String genero;
    private String sinopse;
    private String tipoMidia;

    public ItemMidia() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutorDiretor() { return autorDiretor; }
    public void setAutorDiretor(String autorDiretor) { this.autorDiretor = autorDiretor; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }

    public String getTipoMidia() { return tipoMidia; }
    public void setTipoMidia(String tipoMidia) { this.tipoMidia = tipoMidia; }
}
