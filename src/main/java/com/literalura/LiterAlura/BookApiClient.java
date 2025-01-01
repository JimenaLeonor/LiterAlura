package com.literalura.LiterAlura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class BookApiClient implements CommandLineRunner {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Scanner scanner;
    private final List<Libro> librosBuscados;

    @Autowired
    private AutorRepository autorRepository; // Inyección del repositorio de autores

    @Autowired
    private LibroRepository libroRepository; // Inyección del repositorio de libros

    public BookApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.scanner = new Scanner(System.in);
        this.librosBuscados = new ArrayList<>();
    }

    public HttpResponse<String> buscarLibroPorTitulo(String titulo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://gutendex.com/books?search=" + titulo))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void mostrarMenu() {
        while (true) {
            System.out.println("\n--- Menú ---");
            System.out.println("1. Consultar libro por título");
            System.out.println("2. Ver lista de libros buscados");
            System.out.println("3. Listar autores");
            System.out.println("4. Consultar autores vivos en un año");
            System.out.println("5. Mostrar estadísticas por idioma");
            System.out.println("6. Salir");
            System.out.print("Selecciona una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    consultarLibroPorTitulo();
                    break;
                case 2:
                    mostrarLibrosBuscados();
                    break;
                case 3:
                    listarAutores();
                    break;
                case 4:
                    consultarAutoresVivos();
                    break;
                case 5:
                    mostrarEstadisticasPorIdioma();
                    break;
                case 6:
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción no válida. Intenta de nuevo.");
            }
        }
    }

    private void consultarLibroPorTitulo() {
        System.out.print("Introduce el título del libro: ");
        String titulo = scanner.nextLine();

        try {
            HttpResponse<String> response = buscarLibroPorTitulo(titulo);
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 200) {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode librosNode = jsonNode.path("results");

                if (!librosNode.isEmpty()) {
                    JsonNode libroNode = librosNode.get(0);
                    Libro libro = new Libro();
                    libro.setTitulo(libroNode.path("title").asText());
                    libro.setAutor(libroNode.path("authors").get(0).path("name").asText());
                    libro.setAnio(libroNode.path("year").asInt());
                    libro.setIdioma(libroNode.path("language").asText());
                    libro.setNumeroDescargas(libroNode.path("downloads").asInt());

                    insertarLibro(libro);

                    librosBuscados.add(libro);
                    System.out.println("Libro encontrado:");
                    System.out.println("Título: " + libro.getTitulo());
                    System.out.println("Autor: " + libro.getAutor());
                    System.out.println("Año: " + libro.getAnio());
                    System.out.println("Idioma: " + libro.getIdioma());
                    System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                } else {
                    System.out.println("No se encontraron libros con ese título.");
                }
            } else {
                System.out.println("Error en la solicitud: " + statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertarLibro(Libro libro) {
        Autor autor = new Autor();
        autor.setNombre(libro.getAutor());
        Autor autorGuardado = autorRepository.save(autor);
        libro.setAutor(autorGuardado.getNombre());
        libroRepository.save(libro);
    }

    private void mostrarEstadisticasPorIdioma() {
        System.out.println("Selecciona un idioma para ver la cantidad de libros:");
        System.out.println("1. Español");
        System.out.println("2. Inglés");
        System.out.print("Selecciona una opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();
        String idiomaSeleccionado = "";

        switch (opcion) {
            case 1:
                idiomaSeleccionado = "es";
                break;
            case 2:
                idiomaSeleccionado = "en";
                break;
            default:
                System.out.println("Opción no válida. Regresando al menú.");
                return;
        }

        long cantidadLibros = libroRepository.countByIdioma(idiomaSeleccionado);
        System.out.println("Cantidad de libros en " + idiomaSeleccionado + ": " + cantidadLibros);
    }

    private void mostrarLibrosBuscados() {
        if (librosBuscados.isEmpty()) {
            System.out.println("No has buscado ningún libro todavía.");
        } else {
            System.out.println("Lista de libros buscados:");
            for (Libro libro : librosBuscados) {
                System.out.println("Título: " + libro.getTitulo() + ", Autor: " + libro.getAutor() + ", Año: " + libro.getAnio() + ", Idioma: " + libro.getIdioma() + ", Descargas: " + libro.getNumeroDescargas());
            }
        }
    }

    private void listarAutores() {
        if (librosBuscados.isEmpty()) {
            System.out.println("No has buscado ningún libro, por lo que no hay autores para mostrar.");
        } else {
            System.out.println("Lista de autores de los libros buscados:");
            for (Libro libro : librosBuscados) {
                System.out.println("Autor: " + libro.getAutor());
            }
        }
    }

    private void consultarAutoresVivos() {
        System.out.print("Introduce el año para consultar autores vivos: ");
        int anioConsulta;

        // Validar la entrada del usuario
        while (true) {
            try {
                anioConsulta = scanner.nextInt();
                if (anioConsulta < 0) {
                    throw new IllegalArgumentException("El año no puede ser negativo.");
                }
                break; // Salir del bucle si la entrada es válida
            } catch (Exception e) {
                System.out.println("Entrada inválida. Por favor, introduce un año válido.");
                scanner.nextLine(); // Limpiar el buffer
            }
        }

        List<Autor> autoresVivos = autorRepository.findByAnioNacimientoLessThanEqualAndAnioFallecimientoGreaterThanEqual(anioConsulta, anioConsulta);

        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anioConsulta + ".");
        } else {
            System.out.println("Autores vivos en " + anioConsulta + ":");
            for (Autor autor : autoresVivos) {
                System.out.println("Autor: " + autor.getNombre());
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        mostrarMenu();
    }
}
