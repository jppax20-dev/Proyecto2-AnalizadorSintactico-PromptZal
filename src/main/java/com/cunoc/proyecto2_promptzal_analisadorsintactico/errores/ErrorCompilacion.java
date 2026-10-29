package com.cunoc.proyecto2_promptzal_analisadorsintactico.errores;


public class ErrorCompilacion {

    // Categorias para identificar en que fase del compilador fallo el texto
    public enum Tipo { LEXICO, SINTACTICO, SEMANTICO }

    private final Tipo tipo;
    private final String descripcion;
    private final int fila;
    private final int columna;
    private final String encontrado;
    private final String esperado;

    // Constructor bloqueado para obligar al programa a usar los metodos estaticos de abajo
    private ErrorCompilacion(Tipo tipo, String descripcion, int fila, int columna,
                             String encontrado, String esperado) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fila = fila;
        this.columna = columna;
        this.encontrado = encontrado;
        this.esperado = esperado;
    }

    public static ErrorCompilacion lexico(String descripcion, int fila, int columna) {
        return new ErrorCompilacion(Tipo.LEXICO, descripcion, fila, columna, null, null);
    }

    // Construye un error de escritura armando el mensaje explicativo de forma automatica
    public static ErrorCompilacion sintactico(String encontrado, String esperado, int fila, int columna) {
        String descripcion = "Se encontró " + encontrado + " y se esperaba " + esperado;
        return new ErrorCompilacion(Tipo.SINTACTICO, descripcion, fila, columna, encontrado, esperado);
    }

        public static ErrorCompilacion semantico(String descripcion, int fila, int columna) {
        return new ErrorCompilacion(Tipo.SEMANTICO, descripcion, fila, columna, null, null);
    }

    public Tipo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public String getEncontrado() { return encontrado; }
    public String getEsperado() { return esperado; }
}
