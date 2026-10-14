/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
        tokens.clear();
        errores.clear();
        Lexer lexer = new Lexer(new StringReader(texto), errores);
        try {
            Token token;
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
