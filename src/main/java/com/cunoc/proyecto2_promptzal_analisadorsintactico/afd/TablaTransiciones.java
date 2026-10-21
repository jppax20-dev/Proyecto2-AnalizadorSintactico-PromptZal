package com.cunoc.proyecto2_promptzal_analisadorsintactico.afd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TablaTransiciones {

    public static class Transicion {
        public final String origen;
        public final String simbolo;
        public final String destino;

        public Transicion(String origen, String simbolo, String destino) {
            this.origen = origen;
            this.simbolo = simbolo;
            this.destino = destino;
        }
    }

    private final List<Transicion> transiciones = new ArrayList<>();
    private final Map<String, String> aceptacion = new LinkedHashMap<>();
    private final Map<String, List<String>> reservadas = new LinkedHashMap<>();
    private final String estadoInicial = "q0";
    private final String estadoError = "qerr";

    public TablaTransiciones() {
        construir();
        cargarReservadas();
    }

    private void construir() {
        // Identificadores, palabras reservadas, comandos y conectores de palabra
        agregar("q0", "L", "q1");
        aceptar("q1", "ID");;
        agregar("q1", "D", "q1");
        aceptar("q1", "ID");

        // Directivas: solo @modelo, @rol y @formato, letra por letra
        agregar("q0", "@", "q2");
        agregar("q2", "otro", estadoError);
        palabra("q2", "modelo", 3, "DIRECTIVA");
        palabra("q2", "rol", 9, "DIRECTIVA");
        palabra("q2", "formato", 12, "DIRECTIVA");

        // Enteros y decimales
        agregar("q0", "D", "q19");
        agregar("q19", "D", "q19");
        aceptar("q19", "NUMERO");
        agregar("q19", ".", "q20");
        agregar("q20", "D", "q21");
        agregar("q20", "otro", estadoError);
        agregar("q21", "D", "q21");
        aceptar("q21", "NUMERO");

        // Cadenas
        agregar("q0", "\\\"", "q22");
        agregar("q22", "C", "q22");
        agregar("q22", "\\\"", "q23");
        agregar("q22", "\\\\n", estadoError);
        aceptar("q23", "CADENA");

        // Operadores y flecha
        agregar("q0", "=", "q24");
        aceptar("q24", "IGUAL");
        agregar("q0", "+", "q25");
        aceptar("q25", "MAS");
        agregar("q0", "-", "q26");
        agregar("q26", ">", "q27");
        agregar("q26", "otro", estadoError);
        aceptar("q27", "FLECHA");

        // Delimitadores
        agregar("q0", "{", "q28");
        aceptar("q28", "LLAVE_A");
        agregar("q0", "}", "q29");
        aceptar("q29", "LLAVE_C");
        agregar("q0", "(", "q30");
        aceptar("q30", "PAR_A");
        agregar("q0", ")", "q31");
        aceptar("q31", "PAR_C");
        agregar("q0", ",", "q32");
        aceptar("q32", "COMA");

        // Comentarios: se descartan y el analisis vuelve a q0
        agregar("q0", "/", "q33");
        agregar("q33", "/", "q34");
        agregar("q33", "*", "q35");
        agregar("q33", "otro", estadoError);
        agregar("q34", "N", "q34");
        agregar("q34", "\\\\n", "q0");
        agregar("q35", "B", "q35");
        agregar("q35", "*", "q36");
        agregar("q36", "*", "q36");
        agregar("q36", "X", "q35");
        agregar("q36", "/", "q0");

        // Espacios en blanco: se ignoran
        agregar("q0", "esp/tab", "q0");
        agregar("q0", "\\\\n", "q0");

        agregar("q0", "otro", estadoError);
    }
    
    private void cargarReservadas() {
        reservada("PALABRAS RESERVADAS (cada una, su terminal)",
                "AGENTE → AGENTE", "contexto → CONTEXTO", "variable → VARIABLE",
                "EJECUTAR → EJECUTAR", "EXPORTAR → EXPORTAR");
        reservada("COMANDOS DE IA (COMANDO_IA)",
                "PREGUNTAR", "GENERAR", "RESUMIR", "ANALIZAR", "TRADUCIR", "CLASIFICAR", "EXTRAER");
        reservada("FUNCIÓN DEL SISTEMA (CARGAR)", "CARGAR");
        reservada("CONECTORES (CONECTOR)", "SOBRE", "DESDE", "EN", "COMO");
    }

    private void reservada(String token, String... lexemas) {
        reservadas.put(token, List.of(lexemas));
    }

    public Map<String, List<String>> getReservadas() {
        return reservadas;
    }

    private void palabra(String origen, String texto, int primero, String token) {
        String anterior = origen;
        for (int i = 0; i < texto.length(); i++) {
            String estado = "q" + (primero + i);
            agregar(anterior, String.valueOf(texto.charAt(i)), estado);
            if (i < texto.length() - 1) {
                agregar(estado, "otro", estadoError);
            }
            anterior = estado;
        }
        aceptar(anterior, token);
    }

    private void agregar(String origen, String simbolo, String destino) {
        transiciones.add(new Transicion(origen, simbolo, destino));
    }

    private void aceptar(String estado, String token) {
        aceptacion.put(estado, token);
    }

    public List<Transicion> getTransiciones() {
        return transiciones;
    }

    public Map<String, String> getAceptacion() {
        return aceptacion;
    }

    public boolean esAceptacion(String estado) {
        return aceptacion.containsKey(estado);
    }
    
    
    public String getEstadoInicial() {
        return estadoInicial;
    }

    public String getEstadoError() {
        return estadoError;
    }

    public List<String> getEstados() {
        List<String> estados = new ArrayList<>();
        for (Transicion t : transiciones) {
            if (!estados.contains(t.origen)) estados.add(t.origen);
            if (!estados.contains(t.destino)) estados.add(t.destino);
        }
        return estados;
    }
}