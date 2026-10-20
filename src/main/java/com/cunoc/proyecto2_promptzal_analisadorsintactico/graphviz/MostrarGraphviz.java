package com.cunoc.proyecto2_promptzal_analisadorsintactico.graphviz;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MostrarGraphviz {

    public void compilarImagen(String contenidoDot, Path archivoDot, Path imagenPng)
            throws IOException, InterruptedException {
        if (archivoDot.getParent() != null) {
            Files.createDirectories(archivoDot.getParent());
        }
        Files.writeString(archivoDot, contenidoDot, StandardCharsets.UTF_8);

        ProcessBuilder constructor = new ProcessBuilder(
                "dot", "-Tpng", archivoDot.toString(), "-o", imagenPng.toString());
        constructor.redirectErrorStream(true);
        Process proceso = constructor.start();

        String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int codigo = proceso.waitFor();
        if (codigo != 0) {
            throw new IOException("Graphviz terminó con código " + codigo + ": " + salida);
        }
    }
}