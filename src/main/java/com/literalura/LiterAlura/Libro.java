package com.literalura.LiterAlura;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @ManyToOne(cascade = CascadeType.ALL) // Relación con Autor
    @JoinColumn(name = "autor_id")
    private String autor;

    private int anio;
    private String idioma;
    private int numeroDescargas;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public int getNumeroDescargas() { return numeroDescargas; }
    public void setNumeroDescargas(int numeroDescargas) { this.numeroDescargas = numeroDescargas; }
}

