package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.Simbolo;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TablaSimbolos;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.TipoSimbolo;
import java.util.List;

public class ReporteSimbolos {

    public String generar(TablaSimbolos tabla) {
        StringBuilder sb = new StringBuilder();
        List<Simbolo> simbolos = tabla.getSimbolos();
        if (simbolos.isEmpty()) {
            sb.append("<p>No hay símbolos: el programa no declaró agentes ni variables.</p>\n");
        } else {
            sb.append("<table>\n<tr><th>No.</th><th>Nombre</th><th>Tipo</th>")
                    .append("<th>Ámbito</th><th>Línea</th></tr>\n");
            int numero = 1;
            for (Simbolo s : simbolos) {
                sb.append("<tr>")
                        .append(Html.celda(String.valueOf(numero++)))
                        .append(Html.celda(s.getNombre()))
                        .append(Html.celda(nombre(s.getTipo())))
                        .append(Html.celda(s.getAmbito()))
                        .append(Html.celda(String.valueOf(s.getFila())))
                        .append("</tr>\n");
            }
            sb.append("</table>\n");
        }
        return Html.pagina("Reporte de tabla de símbolos", sb.toString());
    }

    private String nombre(TipoSimbolo tipo) {
        return switch (tipo) {
            case AGENTE -> "Agente";
            case VARIABLE_DECLARADA -> "Variable declarada";
            case RESULTADO_COMANDO -> "Resultado de comando";
        };
    }
}