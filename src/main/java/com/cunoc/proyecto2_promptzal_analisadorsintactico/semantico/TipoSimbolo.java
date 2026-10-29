package com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico;

// Define las unicas tres categorias permitidas en la memoria
public enum TipoSimbolo {
    AGENTE("Agente"),
    VARIABLE_DECLARADA("Variable declarada"),
    RESULTADO_COMANDO("Resultado de comando");

    private final String etiqueta;

    TipoSimbolo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}