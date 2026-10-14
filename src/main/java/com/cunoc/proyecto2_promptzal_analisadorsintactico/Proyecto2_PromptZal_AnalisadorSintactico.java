/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.AnalizadorLexico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import java.nio.file.Files;
import java.nio.file.Path;

public class Proyecto2_PromptZal_AnalisadorSintactico {

    public static void main(String[] args) throws Exception {
        AnalizadorLexico lexico = new AnalizadorLexico();
        lexico.analizar(Files.readString(Path.of("errores.pz")));

        for (Token t : lexico.getTokens()) {
            System.out.println(t.getTipo() + "  '" + t.getLexema() + "'  " + t.getFila() + ":" + t.getColumna());
        }
        for (ErrorCompilacion e : lexico.getErrores()) {
            System.out.println("ERROR " + e.getFila() + ":" + e.getColumna() + "  " + e.getDescripcion());
        }
    }
}