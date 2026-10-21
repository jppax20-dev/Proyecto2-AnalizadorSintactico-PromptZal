package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import java.util.List;

public class ReporteErrores {

    public String generar(List<ErrorCompilacion> errores) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"hud\">\n")
                .append(Html.tarjeta("total", "Total de errores", errores.size()))
                .append(Html.tarjeta("lexico", "Léxicos", contar(errores, ErrorCompilacion.Tipo.LEXICO)))
                .append(Html.tarjeta("sintactico", "Sintácticos", contar(errores, ErrorCompilacion.Tipo.SINTACTICO)))
                .append(Html.tarjeta("semantico", "Semánticos", contar(errores, ErrorCompilacion.Tipo.SEMANTICO)))
                .append("</div>\n");

        if (errores.isEmpty()) {
            sb.append("<div class=\"ok\"><div class=\"titulo\">SIN ERRORES</div>")
                    .append("<p>La compilación no produjo errores léxicos, sintácticos ni semánticos.</p></div>\n");
        } else {
            seccion(sb, ErrorCompilacion.Tipo.LEXICO, "Errores léxicos", errores);
            seccion(sb, ErrorCompilacion.Tipo.SINTACTICO, "Errores sintácticos", errores);
            seccion(sb, ErrorCompilacion.Tipo.SEMANTICO, "Errores semánticos", errores);
        }
        return Html.pagina("Reporte de errores", sb.toString());
    }

    private void seccion(StringBuilder sb, ErrorCompilacion.Tipo tipo, String titulo, List<ErrorCompilacion> errores) {
        List<ErrorCompilacion> delTipo = errores.stream().filter(e -> e.getTipo() == tipo).toList();
        String clase = clase(tipo);
        sb.append("<section class=\"bloque ").append(clase).append("\">\n")
                .append("<h2>").append(titulo).append("<span class=\"cuenta\">").append(delTipo.size()).append("</span></h2>\n");
        if (delTipo.isEmpty()) {
            sb.append("<p class=\"vacio\">Sin errores en esta fase.</p>\n</section>\n");
            return;
        }
        boolean sintactico = tipo == ErrorCompilacion.Tipo.SINTACTICO;
        sb.append("<div class=\"tabla\"><table>\n<tr><th>No.</th><th>Tipo</th><th>Descripción</th>");
        if (sintactico) sb.append("<th>Encontrado</th><th>Esperado</th>");
        sb.append("<th>Fila</th><th>Columna</th></tr>\n");
        int numero = 1;
        for (ErrorCompilacion e : delTipo) {
            sb.append("<tr>")
                    .append(Html.celdaPos(String.valueOf(numero++)))
                    .append(Html.celdaHtml(Html.insignia(clase, etiqueta(tipo))))
                    .append(Html.celda(e.getDescripcion()));
            if (sintactico) {
                sb.append(Html.celdaToken(e.getEncontrado())).append(Html.celdaCodigo(e.getEsperado()));
            }
            sb.append(Html.celdaPos(String.valueOf(e.getFila())))
                    .append(Html.celdaPos(e.getColumna() > 0 ? String.valueOf(e.getColumna()) : "-"))
                    .append("</tr>\n");
        }
        sb.append("</table></div>\n</section>\n");
    }

    private int contar(List<ErrorCompilacion> errores, ErrorCompilacion.Tipo tipo) {
        return (int) errores.stream().filter(e -> e.getTipo() == tipo).count();
    }

    private String clase(ErrorCompilacion.Tipo tipo) {
        return switch (tipo) {
            case LEXICO -> "lexico";
            case SINTACTICO -> "sintactico";
            case SEMANTICO -> "semantico";
        };
    }

    private String etiqueta(ErrorCompilacion.Tipo tipo) {
        return switch (tipo) {
            case LEXICO -> "Léxico";
            case SINTACTICO -> "Sintáctico";
            case SEMANTICO -> "Semántico";
        };
    }
}