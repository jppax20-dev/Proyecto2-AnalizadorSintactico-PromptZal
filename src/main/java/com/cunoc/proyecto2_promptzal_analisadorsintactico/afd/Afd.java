package com.cunoc.proyecto2_promptzal_analisadorsintactico.afd;

import java.util.ArrayList;
import java.util.List;

public class Afd {

    public enum Tipo { NORMAL, ACEPTACION, DESCARTE, ERROR }

    public record Estado(String id, String etiqueta, Tipo tipo) { }

    public record Transicion(String origen, String simbolo, String destino) { }

    private final String inicial;
    private final List<Estado> estados = new ArrayList<>();
    private final List<Transicion> transiciones = new ArrayList<>();

    public Afd(String inicial) {
        this.inicial = inicial;
    }

    public Afd estado(String id, String etiqueta, Tipo tipo) {
        estados.add(new Estado(id, etiqueta, tipo));
        return this;
    }

    public Afd transicion(String origen, String simbolo, String destino) {
        transiciones.add(new Transicion(origen, simbolo, destino));
        return this;
    }

    public String getInicial() { return inicial; }
    public List<Estado> getEstados() { return estados; }
    public List<Transicion> getTransiciones() { return transiciones; }
}