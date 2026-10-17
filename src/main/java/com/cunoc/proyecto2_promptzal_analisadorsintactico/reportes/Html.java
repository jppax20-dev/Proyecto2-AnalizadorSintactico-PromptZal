/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes;

/**
 *
 * @author jppax
 */
final class Html {
    
    private Html() {
    }

    static String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String celda(String texto) {
        return "<td>" + escapar(texto) + "</td>";
    }

    static String pagina(String titulo, String cuerpo) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                %s
                </style>
                </head>
                <body>
                <header><h1>%s</h1></header>
                <main>
                %s
                </main>
                </body>
                </html>
                """.formatted(escapar(titulo), ESTILO, escapar(titulo), cuerpo);
    }
        private static final String ESTILO = """
            body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; background: #f4f6f8; color: #222; }
            header { background: #1f3a5f; color: #fff; padding: 1rem 2rem; }
            header h1 { margin: 0; font-size: 1.4rem; }
            main { padding: 1.5rem 2rem; }
            table { border-collapse: collapse; width: 100%%; background: #fff; }
            th, td { border: 1px solid #d0d5da; padding: 8px 12px; text-align: left; }
            th { background: #e8edf2; }
            tr:nth-child(even) td { background: #f9fafb; }
            .ok { color: #1a7f37; font-weight: bold; }
            """;
}
