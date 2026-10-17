/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import java.util.List;
/**
 *
 * @author jppax
 */
public class ReporteErrores {
    
    public String generar(List<ErrorCompilacion> errores) {
        StringBuilder sb = new StringBuilder();
        if (errores.isEmpty()) {
            sb.append("<p class=\"ok\">La compilación no produjo errores.</p>\n");
        } else {
            sb.append("<p>Total de errores: ").append(errores.size()).append("</p>\n");
            sb.append("<table>\n<tr><th>No.</th><th>Tipo</th><th>Descripción</th>")
                    .append("<th>Fila</th><th>Columna</th><th>Encontrado</th><th>Esperado</th></tr>\n");
            int numero = 1;
            for (ErrorCompilacion e : errores) {
                sb.append("<tr>")
                        .append(Html.celda(String.valueOf(numero++)))
                        .append(Html.celda(nombre(e.getTipo())))
                        .append(Html.celda(e.getDescripcion()))
                        .append(Html.celda(String.valueOf(e.getFila())))
                        .append(Html.celda(e.getColumna() > 0 ? String.valueOf(e.getColumna()) : "-"))
                        .append(Html.celda(valor(e.getEncontrado())))
                        .append(Html.celda(valor(e.getEsperado())))
                        .append("</tr>\n");
            }
            sb.append("</table>\n");
        }
        return Html.pagina("Reporte de errores", sb.toString());
    }

    private String nombre(ErrorCompilacion.Tipo tipo) {
        return switch (tipo) {
            case LEXICO -> "Léxico";
            case SINTACTICO -> "Sintáctico";
            case SEMANTICO -> "Semántico";
        };
    }

    private String valor(String texto) {
        return texto == null ? "-" : texto;
    }
}
