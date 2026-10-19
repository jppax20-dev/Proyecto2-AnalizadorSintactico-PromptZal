package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.Compilador;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.compilador.ResultadoCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteAst;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteErrores;
import com.cunoc.proyecto2_promptzal_analisadorsintactico.reportes.ReporteSimbolos;
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
        panelPrompt.setEditable(false);
        panelErrores.setEditable(false);

        pestanas.addTab("Promptzal", new JScrollPane(panelPrompt));
        pestanas.addTab("Errores", new JScrollPane(panelErrores));

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(editor), pestanas);
        division.setResizeWeight(0.6);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(boton("Abrir", this::abrir));
        barra.add(boton("Guardar", this::guardar));
        barra.add(boton("Compilar", this::compilar));
        barra.add(boton("Guardar promptzal", this::guardarPrompt));
        barra.add(boton("Reportes", this::generarReportes));

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
        panelErrores.setText(sb.length() == 0 ? "Sin errores." : sb.toString());
        pestanas.setTitleAt(1, "Errores (" + ultimo.getErrores().size() + ")");
        panelPrompt.setCaretPosition(0);
        panelErrores.setCaretPosition(0);
        
        
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
}