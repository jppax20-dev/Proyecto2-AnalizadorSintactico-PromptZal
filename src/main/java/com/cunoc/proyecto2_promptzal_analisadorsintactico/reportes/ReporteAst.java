package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.*;
import java.util.List;

public class ReporteAst {

    public String generar(Programa programa) {
        StringBuilder sb = new StringBuilder();
        
        // CSS para convertir listas en un árbol jerárquico visual
        sb.append("<style>\n");
        sb.append("  body { font-family: Arial, sans-serif; background: #f4f6f8; overflow-x: auto; }\n");
        sb.append("  .arbol { white-space: nowrap; margin: 20px; }\n");
        sb.append("  .arbol ul { padding-top: 20px; position: relative; transition: all 0.5s; display: flex; justify-content: center; padding-left: 0; }\n");
        sb.append("  .arbol li { float: left; text-align: center; list-style-type: none; position: relative; padding: 20px 5px 0 5px; }\n");
        sb.append("  .arbol li::before, .arbol li::after { content: ''; position: absolute; top: 0; right: 50%; border-top: 2px solid #ccc; width: 50%; height: 20px; }\n");
        sb.append("  .arbol li::after { right: auto; left: 50%; border-left: 2px solid #ccc; }\n");
        sb.append("  .arbol li:only-child::after, .arbol li:only-child::before { display: none; }\n");
        sb.append("  .arbol li:only-child { padding-top: 0; }\n");
        sb.append("  .arbol li:first-child::before, .arbol li:last-child::after { border: 0 none; }\n");
        sb.append("  .arbol li:last-child::before { border-right: 2px solid #ccc; border-radius: 0 5px 0 0; }\n");
        sb.append("  .arbol li:first-child::after { border-radius: 5px 0 0 0; }\n");
        sb.append("  .arbol ul ul::before { content: ''; position: absolute; top: 0; left: 50%; border-left: 2px solid #ccc; width: 0; height: 20px; }\n");
        
        // Diseño de los nodos (cajas)
        sb.append("  .arbol li span { border: 2px solid #ccc; padding: 8px 15px; text-decoration: none; color: #333; font-size: 12px; display: inline-block; border-radius: 5px; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        sb.append("  .arbol li span b { display: block; margin-bottom: 3px; }\n");
        
        // Colores específicos
        sb.append("  .raiz { border-color: #2c3e50 !important; background-color: #2c3e50 !important; color: white !important; }\n");
        sb.append("  .agente { border-color: #d35400 !important; background-color: #d35400 !important; color: white !important; }\n");
        sb.append("  .comando { border-color: #2980b9 !important; background-color: #2980b9 !important; color: white !important; }\n");
        sb.append("  .variable { border-color: #27ae60 !important; background-color: #27ae60 !important; color: white !important; }\n");
        sb.append("</style>\n");

        sb.append("<div class=\"arbol\">\n");
        sb.append("<ul>\n");
        sb.append("  <li><span class=\"raiz\"><b>Programa</b></span>\n");
        sb.append("    <ul>\n");
        
        for (Directiva d : programa.getDirectivas()) {
            sb.append("<li><span><b>").append(d.getNombre()).append("</b>").append(Html.escapar(d.getValor())).append("</span></li>\n");
        }

        for (Agente a : programa.getAgentes()) {
            agente(sb, a);
        }

        for (Referencia r : programa.getEjecuciones()) {
            sb.append("<li><span><b>Ejecutar</b>").append(Html.escapar(r.getNombre())).append("</span></li>\n");
        }

        for (Referencia r : programa.getExportaciones()) {
            sb.append("<li><span><b>Exportar</b>").append(Html.escapar(r.getNombre())).append("</span></li>\n");
        }

        sb.append("    </ul>\n");
        sb.append("  </li>\n");
        sb.append("</ul>\n");
        sb.append("</div>\n");

        return Html.pagina("AST Visual", sb.toString());
    }

    private void agente(StringBuilder sb, Agente a) {
        String nombre = a.getNombre() == null ? "Sin nombre" : a.getNombre();
        sb.append("<li><span class=\"agente\"><b>Agente</b>").append(Html.escapar(nombre)).append("</span>\n");
        sb.append("<ul>\n");
        
        if (a.getContexto() != null) {
            sb.append("<li><span><b>Contexto</b>\"").append(Html.escapar(a.getContexto())).append("\"</span></li>\n");
        }
        
        for (Variable v : a.getVariables()) {
            sb.append("<li><span class=\"variable\"><b>Variable</b>").append(Html.escapar(v.getNombre())).append("</span>\n");
            sb.append("<ul>\n");
            expresion(sb, v.getValor());
            sb.append("</ul></li>\n");
        }
        
        for (Comando c : a.getComandos()) {
            comando(sb, c);
        }
        
        sb.append("</ul></li>\n");
    }

    private void comando(StringBuilder sb, Comando c) {
        sb.append("<li><span class=\"comando\"><b>").append(Html.escapar(c.getNombre())).append("</b></span>\n");
        sb.append("<ul>\n");
        
        sb.append("<li><span><b>Argumento</b></span><ul>\n");
        expresion(sb, c.getArgumento());
        sb.append("</ul></li>\n");
        
        for (Conector k : c.getConectores()) {
            sb.append("<li><span><b>").append(Html.escapar(k.getNombre())).append("</b></span><ul>\n");
            expresion(sb, k.getArgumento());
            sb.append("</ul></li>\n");
        }
        
        sb.append("<li><span><b>Destino</b>").append(Html.escapar(c.getDestino())).append("</span></li>\n");
        
        sb.append("</ul></li>\n");
    }

    private void expresion(StringBuilder sb, Expresion e) {
        List<Termino> terminos = e.getTerminos();
        if (terminos.size() > 1) {
            sb.append("<li><span><b>+</b></span>\n<ul>\n");
            for (Termino t : terminos) {
                sb.append("<li><span>").append(Html.escapar(descripcion(t))).append("</span></li>\n");
            }
            sb.append("</ul></li>\n");
        } else if (!terminos.isEmpty()) {
            sb.append("<li><span>").append(Html.escapar(descripcion(terminos.get(0)))).append("</span></li>\n");
        }
    }

    private String descripcion(Termino t) {
        if (t instanceof TerminoCadena c) return "\"" + c.getValor() + "\"";
        if (t instanceof TerminoId id) return id.getNombre();
        if (t instanceof TerminoNumero n) {
            return n.getValor() + (n.getUnidad() == null ? "" : " " + n.getUnidad());
        }
        if (t instanceof TerminoCargar c) return "CARGAR(\"" + c.getArchivo() + "\")";
        return "Desconocido";
    }
}