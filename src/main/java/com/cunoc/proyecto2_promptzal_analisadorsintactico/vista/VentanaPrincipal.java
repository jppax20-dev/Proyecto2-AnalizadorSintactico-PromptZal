package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.afd.GeneradorDot;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.afd.TablaTransiciones;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.Compilador;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.ResultadoCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.graphviz.GeneradorDotAst;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.graphviz.MostrarGraphviz;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.lexer.Token;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteAst;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteErrores;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteSimbolos;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.semantico.Simbolo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private final JTextArea editor = new JTextArea();
    private final JTextArea panelPrompt = new JTextArea();
    private final JTextArea panelErrores = new JTextArea();
    private final JTextArea panelTokens = new JTextArea();
    private final JTabbedPane pestanas = new JTabbedPane();
    private final Compilador compilador = new Compilador();
    private ResultadoCompilacion ultimo;
    private Path archivoActual;

    public VentanaPrincipal() {
        super("PromptZal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        construir();
    }

    private void construir() {
        Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        for (JTextArea area : new JTextArea[] {editor, panelPrompt, panelErrores, panelTokens}) {
            area.setFont(fuente);
        }
        panelPrompt.setEditable(false);
        panelErrores.setEditable(false);
        panelTokens.setEditable(false);

        pestanas.addTab("Promptzal", new JScrollPane(panelPrompt));
        pestanas.addTab("Errores", new JScrollPane(panelErrores));
        pestanas.addTab("Tokens", new JScrollPane(panelTokens));
        pestanas.addTab("Símbolos", scrollSimbolos);
        panelPrompt.setEditable(false);
        panelErrores.setEditable(false);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(editor), pestanas);
        division.setResizeWeight(0.6);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(boton("Abrir", this::abrir));
        barra.add(boton("Guardar", this::guardar));
        barra.add(boton("Compilar", this::compilar));
        barra.add(boton("Guardar promptzal", this::guardarPrompt));
        barra.add(boton("Reportes", this::generarReportes));
        barra.add(boton("AFD", this::generarAfd));

        add(barra, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
    }

    private JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void abrir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path ruta = chooser.getSelectedFile().toPath();
        try {
            editor.setText(Files.readString(ruta));
            archivoActual = ruta;
            setTitle("PromptZal - " + ruta.getFileName());
        } catch (IOException ex) {
            mensaje("No se pudo abrir el archivo: " + ex.getMessage());
        }
    }

    private void guardar() {
        if (archivoActual == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            archivoActual = conExtension(chooser.getSelectedFile().toPath(), ".pz");
            setTitle("PromptZal - " + archivoActual.getFileName());
        }
        escribir(archivoActual, editor.getText());
    }

    private void compilar() {
        ultimo = compilador.compilar(editor.getText());
        panelPrompt.setText(ultimo.getPromptzal());

        StringBuilder sb = new StringBuilder();
        for (ErrorCompilacion e : ultimo.getErrores()) {
            sb.append(e.getTipo()).append(" [").append(e.getFila()).append(':')
                    .append(e.getColumna()).append("] ").append(e.getDescripcion()).append('\n');
        }
        panelErrores.setText(textoErrores(ultimo.getErrores()));
        pestanas.setTitleAt(1, "Errores (" + ultimo.getErrores().size() + ")");
        panelPrompt.setCaretPosition(0);
        panelErrores.setCaretPosition(0);

        StringBuilder tokens = new StringBuilder(String.format("%-14s %-32s %s%n", "TIPO", "LEXEMA", "FILA:COL"));
        for (Token t : ultimo.getTokens()) {
            tokens.append(String.format("%-14s %-32s %d:%d%n", t.getTipo(), t.getLexema(), t.getFila(), t.getColumna()));
        }
        panelTokens.setText(tokens.toString());
        pestanas.setTitleAt(2, "Tokens (" + ultimo.getTokens().size() + ")");
        panelTokens.setCaretPosition(0);
        
        GeneradorDotAst generadorAst = new GeneradorDotAst();
        String codigoDot = generadorAst.generarCodigo(ultimo.getPrograma());

        try {
            new MostrarGraphviz().compilarImagen(
            new GeneradorDotAst().generarCodigo(ultimo.getPrograma()),
            Path.of("reportes/ast.dot"), Path.of("reportes/ast.png"));
        } catch (IOException ex) {
            mensaje("No se pudo generar la imagen del AST:\n" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        modeloSimbolos.setRowCount(0);
        for (Simbolo s : ultimo.getTabla().getSimbolos()) {
            modeloSimbolos.addRow(new Object[] {s.getNombre(), s.getTipo().getEtiqueta(), s.getAmbito(), s.getFila()});
        }
        pestanas.setTitleAt(pestanas.indexOfComponent(scrollSimbolos),
                "Símbolos (" + ultimo.getTabla().getSimbolos().size() + ")");
    }

    private void guardarPrompt() {
        if (ultimo == null) {
            mensaje("Compila un programa primero.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Texto (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        escribir(conExtension(chooser.getSelectedFile().toPath(), ".md", ".txt"), ultimo.getPromptzal());
    }

    private void generarReportes() {
        if (ultimo == null) {
            mensaje("Compila un programa primero.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, "Elegir carpeta") != JFileChooser.APPROVE_OPTION) return;
        Path carpeta = chooser.getSelectedFile().toPath();

        boolean ok = escribir(carpeta.resolve("reporte_errores.html"),
                new ReporteErrores().generar(ultimo.getErrores()));
        ok &= escribir(carpeta.resolve("reporte_simbolos.html"),
                new ReporteSimbolos().generar(ultimo.getTabla()));
        ok &= escribir(carpeta.resolve("reporte_ast.html"),
                new ReporteAst().generar(ultimo.getPrograma()));
        if (ok) mensaje("Reportes generados en: " + carpeta);
    }

    private boolean escribir(Path ruta, String contenido) {
        try {
            Files.writeString(ruta, contenido);
            return true;
        } catch (IOException ex) {
            mensaje("No se pudo guardar el archivo: " + ex.getMessage());
            return false;
        }
    }

    private Path conExtension(Path ruta, String... validas) {
        String nombre = ruta.getFileName().toString().toLowerCase();
        for (String extension : validas) {
            if (nombre.endsWith(extension)) return ruta;
        }
        return ruta.resolveSibling(ruta.getFileName() + validas[0]);
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
    
    private String textoErrores(List<ErrorCompilacion> errores) {
        if (errores.isEmpty()) return "Sin errores.";
        StringBuilder sb = new StringBuilder();
        seccion(sb, "ERRORES LÉXICOS", errores, ErrorCompilacion.Tipo.LEXICO);
        seccion(sb, "ERRORES SINTÁCTICOS", errores, ErrorCompilacion.Tipo.SINTACTICO);
        seccion(sb, "ERRORES SEMÁNTICOS", errores, ErrorCompilacion.Tipo.SEMANTICO);
        return sb.toString();
    }

    private void seccion(StringBuilder sb, String titulo, List<ErrorCompilacion> errores,
                         ErrorCompilacion.Tipo tipo) {
        List<ErrorCompilacion> delTipo = errores.stream().filter(e -> e.getTipo() == tipo).toList();
        if (delTipo.isEmpty()) return;
        if (sb.length() > 0) sb.append('\n');
        sb.append(titulo).append(" (").append(delTipo.size()).append(")\n");
        for (ErrorCompilacion e : delTipo) {
            sb.append("  [").append(e.getFila()).append(':').append(e.getColumna()).append("] ")
                    .append(e.getDescripcion()).append('\n');
        }
    }
    
        private void generarAfd() {
        try {
            new MostrarGraphviz().compilarImagen(
        new GeneradorDot(new TablaTransiciones()).generarDot(),
        Path.of("reportes/afd.dot"), Path.of("reportes/afd.png"));
        } catch (IOException ex) {
            mensaje("No se pudo generar el AFD:\n" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
        
        private final DefaultTableModel modeloSimbolos = new DefaultTableModel(
            new String[] {"Nombre", "Tipo", "Ámbito", "Línea"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JScrollPane scrollSimbolos = new JScrollPane(new JTable(modeloSimbolos));
   
}