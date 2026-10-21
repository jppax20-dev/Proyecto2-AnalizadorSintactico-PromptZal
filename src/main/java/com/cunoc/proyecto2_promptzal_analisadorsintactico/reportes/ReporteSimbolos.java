package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.Simbolo;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TipoSimbolo;
import java.util.List;

public class ReporteSimbolos {

    public String generar(TablaSimbolos tabla) {
        List<Simbolo> simbolos = tabla.getSimbolos();
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"hud\">\n")
                .append(Html.tarjeta("total", "Símbolos", simbolos.size()))
                .append(Html.tarjeta("agente", "Agentes", contar(simbolos, TipoSimbolo.AGENTE)))
                .append(Html.tarjeta("variable", "Variables declaradas", contar(simbolos, TipoSimbolo.VARIABLE_DECLARADA)))
                .append(Html.tarjeta("resultado", "Resultados de comando", contar(simbolos, TipoSimbolo.RESULTADO_COMANDO)))
                .append("</div>\n");

        if (simbolos.isEmpty()) {
            sb.append("<p class=\"vacio\">No hay símbolos: el programa no declaró agentes ni variables.</p>\n");
        } else {
            sb.append("<section class=\"bloque total\">\n<h2>Tabla de símbolos</h2>\n")
                    .append("<div class=\"tabla\"><table>\n<tr><th>No.</th><th>Nombre</th><th>Tipo</th>")
                    .append("<th>Ámbito</th><th>Línea</th></tr>\n");
            int numero = 1;
            for (Simbolo s : simbolos) {
                sb.append("<tr>")
                        .append(Html.celdaPos(String.valueOf(numero++)))
                        .append(Html.celdaCodigo(s.getNombre()))
                        .append(Html.celdaHtml(Html.insignia(clase(s.getTipo()), s.getTipo().getEtiqueta())))
                        .append(Html.celdaCodigo(s.getAmbito()))
                        .append(Html.celdaPos(String.valueOf(s.getFila())))
                        .append("</tr>\n");
            }
            sb.append("</table></div>\n</section>\n");
        }
        return Html.pagina("Reporte de tabla de símbolos", sb.toString());
    }

    private int contar(List<Simbolo> simbolos, TipoSimbolo tipo) {
        return (int) simbolos.stream().filter(s -> s.getTipo() == tipo).count();
    }

    private String clase(TipoSimbolo tipo) {
        return switch (tipo) {
            case AGENTE -> "agente";
            case VARIABLE_DECLARADA -> "variable";
            case RESULTADO_COMANDO -> "resultado";
        };
    }
}