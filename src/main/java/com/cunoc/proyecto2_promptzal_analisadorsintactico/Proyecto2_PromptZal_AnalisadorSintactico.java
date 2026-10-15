/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Agente;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.Programa;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.AnalizadorLexico;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.parser.Parser;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class Proyecto2_PromptZal_AnalisadorSintactico {

        public static void main(String[] args) throws Exception {
        AnalizadorLexico lexico = new AnalizadorLexico();
        lexico.analizar(Files.readString(Path.of("mi.pz")));

        List<ErrorCompilacion> errores = new ArrayList<>(lexico.getErrores());
        Programa programa = new Parser(lexico.getTokens(), errores).parsear();

        for (ErrorCompilacion e : errores) {
            System.out.println(e.getTipo() + " " + e.getFila() + ":" + e.getColumna() + "  " + e.getDescripcion());
        }
        System.out.println("Directivas: " + programa.getDirectivas().size());
        for (Agente a : programa.getAgentes()) {
            System.out.println("Agente " + a.getNombre() + ": " + a.getVariables().size()
                    + " variables, " + a.getComandos().size() + " comandos");
        }
        System.out.println("Ejecuciones: " + programa.getEjecuciones().size());
        System.out.println("Exportaciones: " + programa.getExportaciones().size());
    }
}