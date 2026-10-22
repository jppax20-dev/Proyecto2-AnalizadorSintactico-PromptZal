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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private final JTextArea editor = new JTextArea();
    private final JTextArea lineas = new JTextArea(" 1 ");
    private final JTextArea panelPrompt = new JTextArea();
    private final JTabbedPane pestanasEditor = new JTabbedPane();
    private final JTabbedPane pestanasInferior = new JTabbedPane();
    private final JTabbedPane pestanasIzquierda = new JTabbedPane();
    private final JLabel etiquetaEstado = new JLabel(" Listo");
    private final JLabel etiquetaCursor = new JLabel("1 : 1 ");
    private final Compilador compilador = new Compilador();
    private ResultadoCompilacion ultimo;
    private Path archivoActual;

    private final DefaultTableModel modeloSimbolos = crearModelo("Nombre", "Tipo", "Ámbito", "Línea");
    private final DefaultTableModel modeloTokens = crearModelo("Tipo", "Lexema", "Fila", "Columna");
    private final DefaultTableModel modeloErrores = crearModelo("Tipo", "Fila", "Columna", "Descripción");

    private final JScrollPane scrollSimbolos = new JScrollPane(new JTable(modeloSimbolos));
    private final JScrollPane scrollTokens = new JScrollPane(new JTable(modeloTokens));
    private final JScrollPane scrollErrores = new JScrollPane(new JTable(modeloErrores));

    public VentanaPrincipal() {
        super("PromptZal IDE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        construir();
    }

    private DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void construir() {
        Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        editor.setFont(fuente);
        lineas.setFont(fuente);
        panelPrompt.setFont(fuente);

        lineas.setEditable(false);
        lineas.setFocusable(false);
        panelPrompt.setEditable(false);

        JToolBar barraSuperior = new JToolBar();
        barraSuperior.setFloatable(false);
        barraSuperior.add(boton("Guardar", this::guardar));
        barraSuperior.addSeparator();
        barraSuperior.add(boton("Compilar", this::compilar));
        barraSuperior.add(boton("Guardar promptzal", this::guardarPrompt));
        barraSuperior.addSeparator();
        barraSuperior.add(boton("Reportes", this::generarReportes));

        JPanel panelBotonesIzquierda = new JPanel(new GridLayout(0, 1, 0, 5));
        panelBotonesIzquierda.add(boton("Abrir", this::abrir));
        panelBotonesIzquierda.add(boton("AFD", this::generarAfd));

        JPanel contenedorIzquierda = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(8, 8, 8, 8);
        contenedorIzquierda.add(panelBotonesIzquierda, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        contenedorIzquierda.add(new JPanel(), gbc);

        pestanasIzquierda.addTab("Explorador", contenedorIzquierda);
        pestanasIzquierda.setMinimumSize(new Dimension(150, 0));

        JScrollPane scrollEditor = new JScrollPane(editor);
        scrollEditor.setRowHeaderView(lineas);
        pestanasEditor.addTab("SinTitulo.pz", scrollEditor);

        pestanasInferior.addTab("Promptzal", new JScrollPane(panelPrompt));
        pestanasInferior.addTab("Errores (0)", scrollErrores);
        pestanasInferior.addTab("Tokens (0)", scrollTokens);
        pestanasInferior.addTab("Símbolos (0)", scrollSimbolos);

        JSplitPane divisionVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestanasEditor, pestanasInferior);
        divisionVertical.setResizeWeight(0.7);

        JSplitPane divisionHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pestanasIzquierda, divisionVertical);
        divisionHorizontal.setDividerLocation(180);

        JPanel barraEstado = new JPanel(new BorderLayout());
        barraEstado.setBorder(BorderFactory.createEtchedBorder());
        barraEstado.add(etiquetaEstado, BorderLayout.WEST);
        barraEstado.add(etiquetaCursor, BorderLayout.EAST);

        editor.addCaretListener(e -> actualizarCursor());
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarLineas();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarLineas();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarLineas();
            }
        });

        add(barraSuperior, BorderLayout.NORTH);
        add(divisionHorizontal, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);
    }

    private void actualizarLineas() {
        int total = editor.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= total; i++) {
            sb.append(" ").append(i).append(" \n");
        }
        lineas.setText(sb.toString());
    }

    private JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFocusable(false);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void actualizarCursor() {
        try {
            int pos = editor.getCaretPosition();
            int fila = editor.getLineOfOffset(pos);
            int col = pos - editor.getLineStartOffset(fila);
            etiquetaCursor.setText((fila + 1) + " : " + (col + 1) + " ");
        } catch (Exception ex) {
            etiquetaCursor.setText("1 : 1 ");
        }
    }

    private void abrir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path ruta = chooser.getSelectedFile().toPath();
        try {
            editor.setText(Files.readString(ruta));
            archivoActual = ruta;
            String nombre = ruta.getFileName().toString();
            setTitle("PromptZal IDE - " + nombre);
            pestanasEditor.setTitleAt(0, nombre);
            etiquetaEstado.setText(" Archivo cargado: " + nombre);
            editor.setCaretPosition(0);
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
            String nombre = archivoActual.getFileName().toString();
            setTitle("PromptZal IDE - " + nombre);
            pestanasEditor.setTitleAt(0, nombre);
        }
        if (escribir(archivoActual, editor.getText())) {
            etiquetaEstado.setText(" Guardado: " + archivoActual.getFileName());
        }
    }

    private void compilar() {
        ultimo = compilador.compilar(editor.getText());
        panelPrompt.setText(ultimo.getPromptzal());
        panelPrompt.setCaretPosition(0);

        modeloErrores.setRowCount(0);
        for (ErrorCompilacion e : ultimo.getErrores()) {
            modeloErrores.addRow(new Object[]{e.getTipo(), e.getFila(), e.getColumna(), e.getDescripcion()});
        }
        pestanasInferior.setTitleAt(1, "Errores (" + ultimo.getErrores().size() + ")");

        modeloTokens.setRowCount(0);
        for (Token t : ultimo.getTokens()) {
            modeloTokens.addRow(new Object[]{t.getTipo(), t.getLexema(), t.getFila(), t.getColumna()});
        }
        pestanasInferior.setTitleAt(2, "Tokens (" + ultimo.getTokens().size() + ")");

        modeloSimbolos.setRowCount(0);
        for (Simbolo s : ultimo.getTabla().getSimbolos()) {
            modeloSimbolos.addRow(new Object[]{s.getNombre(), s.getTipo().getEtiqueta(), s.getAmbito(), s.getFila()});
        }
        pestanasInferior.setTitleAt(3, "Símbolos (" + ultimo.getTabla().getSimbolos().size() + ")");

        try {
            new MostrarGraphviz().compilarImagen(
                    new GeneradorDotAst().generarCodigo(ultimo.getPrograma()),
                    Path.of("reportes/ast.dot"), Path.of("reportes/ast.png"));
        } catch (IOException ex) {
            mensaje("No se pudo generar la imagen del AST:\n" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        if (ultimo.getErrores().isEmpty()) {
            etiquetaEstado.setText(" Compilación exitosa");
            pestanasInferior.setSelectedIndex(0);
        } else {
            etiquetaEstado.setText(" Compilación terminada con " + ultimo.getErrores().size() + " errores");
            pestanasInferior.setSelectedIndex(1);
        }
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
        if (ok) {
            etiquetaEstado.setText(" Reportes generados en: " + carpeta);
            mensaje("Reportes generados en: " + carpeta);
        }
    }

    private void generarAfd() {
        try {
            new MostrarGraphviz().compilarImagen(
                    new GeneradorDot(new TablaTransiciones()).generarDot(),
                    Path.of("reportes/afd.dot"), Path.of("reportes/afd.png"));
            etiquetaEstado.setText(" Imagen AFD generada en reportes/afd.png");
        } catch (IOException ex) {
            mensaje("No se pudo generar el AFD:\n" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
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