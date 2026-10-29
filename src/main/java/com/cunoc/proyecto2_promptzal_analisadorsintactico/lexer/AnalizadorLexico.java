package com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorLexico {

    private final List<Token> tokens = new ArrayList<>();
    private final List<ErrorCompilacion> errores = new ArrayList<>();

    public void analizar(String texto) {
        // Vacia las listas por si el usuario compila un texto nuevo
        tokens.clear();
        errores.clear();
        Lexer lexer = new Lexer(new StringReader(texto), errores);
        try {
            Token token;
            // Extrae todas las piezas de texto hasta detectar el final del archivo
            do {
                token = lexer.yylex();
                tokens.add(token);
            } while (token.getTipo() != TipoToken.EOF);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el texto", e);
        }
    }

    public List<Token> getTokens() { return tokens; }
    public List<ErrorCompilacion> getErrores() { return errores; }
}
