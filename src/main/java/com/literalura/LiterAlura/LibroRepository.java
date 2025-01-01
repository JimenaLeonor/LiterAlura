package com.literalura.LiterAlura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    long countByIdioma(String idioma);
    List<Libro> findByIdioma(String idioma);
}
