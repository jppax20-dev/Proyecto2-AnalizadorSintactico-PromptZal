package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.ast.*;
import java.util.List;

public class ReporteAst {

        private static final String ESTILO = """
            :root {
              --celeste: #6ec1ff;
              --lima: #c8ff6e;
              --amarillo: #ffd166;
              --linea: #2f5d7c;
            }
            main { max-width: none; }
            .cabecera { max-width: none; }
            .hud { max-width: 1052px; }
            .raiz { --acento: #ffffff; }
            .grupo { --acento: var(--tenue); }
            .comando { --acento: var(--magenta); }
            .parte { --acento: var(--violeta); }
            .cadena { --acento: var(--celeste); }
            .id { --acento: var(--texto); }
            .numero { --acento: var(--amarillo); }
            .cargar { --acento: var(--lima); }
            .arbol-caja {
              padding: 28px 24px 34px;
              overflow-x: auto;
              background: var(--panel);
              border: 1px solid var(--borde);
              border-radius: 8px;
              box-shadow: 0 0 18px rgba(0, 0, 0, 0.4);
              scrollbar-color: var(--cian) var(--panel2);
            }
            .arbol { width: max-content; margin: 0 auto; }
            .arbol ul {
              display: flex;
              justify-content: center;
              margin: 0;
              padding: 20px 0 0;
              position: relative;
              list-style: none;
            }
            .arbol > ul { padding-top: 0; }
            .arbol li {
              flex: none;
              position: relative;
              padding: 20px 8px 0;
              text-align: center;
              list-style: none;
            }
            .arbol li::before, .arbol li::after {
              content: '';
              position: absolute;
              top: 0;
              right: 50%;
              width: 50%;
              height: 20px;
              border-top: 2px solid var(--linea);
            }
            .arbol li::after { right: auto; left: 50%; border-left: 2px solid var(--linea); }
            .arbol li:only-child::before, .arbol li:only-child::after { display: none; }
            .arbol li:only-child { padding-top: 0; }
            .arbol li:first-child::before, .arbol li:last-child::after { border: 0 none; }
            .arbol li:last-child::before { border-right: 2px solid var(--linea); border-radius: 0 6px 0 0; }
            .arbol li:first-child::after { border-radius: 6px 0 0 0; }
            .arbol ul ul::before {
              content: '';
              position: absolute;
              top: 0;
              left: 50%;
              width: 0;
              height: 20px;
              border-left: 2px solid var(--linea);
            }
            .nodo {
              display: inline-block;
              max-width: 260px;
              padding: 8px 14px;
              border: 1px solid var(--acento, var(--cian));
              border-radius: 6px;
              background: var(--panel);
              color: var(--texto);
              font-size: 0.8rem;
              line-height: 1.35;
              white-space: normal;
              overflow-wrap: break-word;
              box-shadow: 0 0 10px rgba(0, 0, 0, 0.45);
            }
            .nodo.raiz, .nodo.agente, .nodo.variable, .nodo.comando, .nodo.resultado {
              border-width: 2px;
              box-shadow: 0 0 12px var(--acento);
            }
            .nodo.raiz { box-shadow: 0 0 14px var(--cian); }
            .nodo b {
              display: block;
              white-space: nowrap;
              color: var(--acento, var(--cian));
              font-family: Consolas, 'Courier New', monospace;
              font-size: 0.72rem;
              letter-spacing: 0.06em;
            }
            .nodo .valor { display: block; font-family: Consolas, 'Courier New', monospace; }
            .nodo small { display: block; margin-top: 2px; color: var(--tenue); font-size: 0.68rem; white-space: nowrap; }
            .leyenda { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
            """;

    public String generar(Programa programa) {
        StringBuilder sb = new StringBuilder();
        sb.append(resumen(programa));
        sb.append("<section class=\"bloque total\">\n<h2>Árbol de sintaxis abstracta</h2>\n");
        sb.append("<div class=\"arbol-caja\"><div class=\"arbol\">\n<ul>\n<li>")
                .append(nodo("raiz", "Programa", null)).append("\n<ul>\n");
        
        // Dibuja la rama de configuracion inicial si el usuario declaro directivas
        if (!programa.getDirectivas().isEmpty()) {
            sb.append("<li>").append(nodo("grupo", "Directivas", null)).append("\n<ul>\n");
            for (Directiva d : programa.getDirectivas()) {
                sb.append("<li>")
                        .append(nodo("cadena", d.getNombre(), "\"" + d.getValor() + "\"", linea(d.getFila())))
                        .append("</li>\n");
            }
            sb.append("</ul></li>\n");
        }
        if (!programa.getAgentes().isEmpty()) {
            sb.append("<li>").append(nodo("grupo", "Agentes", null)).append("\n<ul>\n");
            for (Agente a : programa.getAgentes()) {
                agente(sb, a);
            }
            sb.append("</ul></li>\n");
        }
        referencias(sb, "Ejecuciones", "Ejecutar", programa.getEjecuciones());
        referencias(sb, "Exportaciones", "Exportar", programa.getExportaciones());

        sb.append("</ul></li>\n</ul>\n</div></div>\n").append(leyenda()).append("</section>\n");
        return Html.pagina("Reporte del AST", sb.toString(), ESTILO);
    }

    private String resumen(Programa programa) {
        int variables = 0;
        int comandos = 0;
        for (Agente a : programa.getAgentes()) {
            variables += a.getVariables().size();
            comandos += a.getComandos().size();
        }
        return "<div class=\"hud\">\n"
                + Html.tarjeta("agente", "Agentes", programa.getAgentes().size())
                + Html.tarjeta("variable", "Variables", variables)
                + Html.tarjeta("comando", "Comandos", comandos)
                + Html.tarjeta("resultado", "Ejecuciones", programa.getEjecuciones().size())
                + "</div>\n";
    }

    private void agente(StringBuilder sb, Agente a) {
        String nombre = a.getNombre() == null ? "(sin nombre)" : a.getNombre();
        sb.append("<li>").append(nodo("agente", "Agente", nombre, linea(a.getFila())));
        // Revisa si el agente esta vacio o tiene instrucciones adentro para dibujar sus ramas
        boolean hijos = a.getContexto() != null || !a.getVariables().isEmpty() || !a.getComandos().isEmpty();
        if (hijos) {
            sb.append("\n<ul>\n");
            if (a.getContexto() != null) {
                sb.append("<li>").append(nodo("cadena", "Contexto", "\"" + a.getContexto() + "\"")).append("</li>\n");
            }
            for (Variable v : a.getVariables()) {
                sb.append("<li>").append(nodo("variable", "Variable", v.getNombre(), linea(v.getFila())))
                        .append("\n<ul>\n");
                expresion(sb, v.getValor());
                sb.append("</ul></li>\n");
            }
            for (Comando c : a.getComandos()) {
                comando(sb, c);
            }
            sb.append("</ul>");
        }
        sb.append("</li>\n");
    }

    private void comando(StringBuilder sb, Comando c) {
        sb.append("<li>").append(nodo("comando", c.getNombre(), null, linea(c.getFila()))).append("\n<ul>\n");
        // Dibuja el parametro principal que todo comando debe tener por obligacion
        sb.append("<li>").append(nodo("parte", "Argumento", null)).append("\n<ul>\n");
        expresion(sb, c.getArgumento());
        sb.append("</ul></li>\n");
        for (Conector k : c.getConectores()) {
            sb.append("<li>").append(nodo("parte", k.getNombre(), null)).append("\n<ul>\n");
            expresion(sb, k.getArgumento());
            sb.append("</ul></li>\n");
        }
        sb.append("<li>").append(nodo("resultado", "Destino", c.getDestino(), linea(c.getFilaDestino())))
                .append("</li>\n");
        sb.append("</ul></li>\n");
    }

    private void expresion(StringBuilder sb, Expresion e) {
        List<Termino> terminos = e.getTerminos();
        // Si hay varias piezas unidas por el signo mas agrupa todas bajo un mismo nodo
        if (terminos.size() > 1) {
            sb.append("<li>").append(nodo("parte", "Concatenación (+)", null)).append("\n<ul>\n");
            for (Termino t : terminos) {
                sb.append("<li>").append(termino(t)).append("</li>\n");
            }
            sb.append("</ul></li>\n");
        } else if (!terminos.isEmpty()) {
            sb.append("<li>").append(termino(terminos.get(0))).append("</li>\n");
        }
    }

    private String termino(Termino t) {
        if (t instanceof TerminoCadena c) return nodo("cadena", "Cadena", "\"" + c.getValor() + "\"");
        if (t instanceof TerminoId id) return nodo("id", "Identificador", id.getNombre());
        if (t instanceof TerminoNumero n) {
            return nodo("numero", "Número", n.getValor(), n.getUnidad() == null ? null : "unidad: " + n.getUnidad());
        }
        if (t instanceof TerminoCargar c) return nodo("cargar", "CARGAR", "\"" + c.getArchivo() + "\"");
        return nodo("parte", "Desconocido", null);
    }

    private void referencias(StringBuilder sb, String grupo, String etiqueta, List<Referencia> lista) {
        if (lista.isEmpty()) return;
        sb.append("<li>").append(nodo("grupo", grupo, null)).append("\n<ul>\n");
        for (Referencia r : lista) {
            sb.append("<li>").append(nodo("id", etiqueta, r.getNombre(), linea(r.getFila()))).append("</li>\n");
        }
        sb.append("</ul></li>\n");
    }

    private String leyenda() {
        return "<div class=\"leyenda\">"
                + Html.insignia("agente", "Agente") + Html.insignia("variable", "Variable")
                + Html.insignia("comando", "Comando") + Html.insignia("parte", "Argumento / conector")
                + Html.insignia("resultado", "Destino") + Html.insignia("cadena", "Cadena")
                + Html.insignia("id", "Identificador") + Html.insignia("numero", "Número")
                + Html.insignia("cargar", "CARGAR") + "</div>\n";
    }

    private String linea(int fila) {
        return "línea " + fila;
    }

    private String nodo(String clase, String etiqueta, String valor) {
        return nodo(clase, etiqueta, valor, null);
    }

    private String nodo(String clase, String etiqueta, String valor, String nota) {
        StringBuilder sb = new StringBuilder("<span class=\"nodo ").append(clase).append("\"><b>")
                .append(Html.escapar(etiqueta)).append("</b>");
        if (valor != null) sb.append("<span class=\"valor\">").append(Html.escapar(valor)).append("</span>");
        if (nota != null) sb.append("<small>").append(Html.escapar(nota)).append("</small>");
        return sb.append("</span>").toString();
    }
}