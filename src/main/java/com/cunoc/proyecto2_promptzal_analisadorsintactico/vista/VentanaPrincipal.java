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
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private static final String TITULO_VENTANA = "PromptZal IDE";
    private static final Color COLOR_EXITO = new Color(198, 239, 206);
    private static final Color COLOR_ERROR = new Color(255, 199, 206);
    private static final String TEXTO_AST_VACIO = "Compila un programa para ver su AST";
    private static final FileNameExtensionFilter FILTRO_PZ =
            new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz");

    private final Compilador compilador = new Compilador();
    private final Image fondo;
    private final Map<PestanaEditor, JLabel> titulos = new HashMap<>();

    private final JTabbedPane pestanasEditor = new JTabbedPane();
    private final JTabbedPane pestanasInferior = new JTabbedPane();
    private final JLabel etiquetaEstado = new JLabel(" Listo");
    private final JLabel etiquetaCursor = new JLabel("1 : 1 ");

    private final JTextArea panelPrompt = new JTextArea();
    private final JLabel etiquetaAst = new JLabel(TEXTO_AST_VACIO, JLabel.CENTER);

    private final DefaultTableModel modeloTokens = crearModelo("No.", "Lexema", "Tipo", "Fila", "Columna");
    private final DefaultTableModel modeloErrores = crearModelo("No.", "Tipo", "Descripción", "Fila", "Columna");
    private final DefaultTableModel modeloSimbolos = crearModelo("Nombre", "Tipo", "Ámbito", "Línea");
    private final DefaultTableModel modeloEstadisticas = crearModelo("Métrica", "Valor");

    private final JTable tablaTokens = new JTable(modeloTokens);
    private final JTable tablaErrores = new JTable(modeloErrores);
    private final JTable tablaSimbolos = new JTable(modeloSimbolos);
    private final JTable tablaEstadisticas = new JTable(modeloEstadisticas);

    private final JScrollPane scrollPrompt = new JScrollPane(panelPrompt);
    private final JScrollPane scrollTokens = new JScrollPane(tablaTokens);
    private final JScrollPane scrollErrores = new JScrollPane(tablaErrores);
    private final JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
    private final JScrollPane scrollEstadisticas = new JScrollPane(tablaEstadisticas);
    private final JScrollPane scrollAst = new JScrollPane(etiquetaAst);

    private ResultadoCompilacion ultimo;
    private PestanaEditor pestanaCompilada;
    private File ultimoDirectorio;
    private int generacionAst;

    public VentanaPrincipal() {
        super(TITULO_VENTANA);
        fondo = cargarFondo();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        construir();
        registrarAtajos();
        nuevaPestana();
    }

    private Image cargarFondo() {
        URL recurso = getClass().getResource("/logo_promptzal.png");
        if (recurso == null) return null;
        try {
            return ImageIO.read(recurso);
        } catch (IOException ex) {
            return null;
        }
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
        configurarTablas();
        panelPrompt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        panelPrompt.setEditable(false);
        panelPrompt.setLineWrap(true);
        panelPrompt.setWrapStyleWord(true);
        panelPrompt.setMargin(new Insets(6, 8, 6, 8));

        pestanasEditor.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        pestanasEditor.addChangeListener(e -> alCambiarPestana());

        pestanasInferior.addTab("Promptzal", scrollPrompt);
        pestanasInferior.addTab("Tokens", scrollTokens);
        pestanasInferior.addTab("Errores", scrollErrores);
        pestanasInferior.addTab("Símbolos", scrollSimbolos);
        pestanasInferior.addTab("Estadísticas", scrollEstadisticas);
        pestanasInferior.addTab("AST", scrollAst);

        JSplitPane divisionVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestanasEditor, pestanasInferior);
        divisionVertical.setResizeWeight(0.7);
        divisionVertical.setContinuousLayout(true);

        JSplitPane divisionHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelExplorador(), divisionVertical);
        divisionHorizontal.setDividerLocation(180);
        divisionHorizontal.setContinuousLayout(true);

        add(barraHerramientas(), BorderLayout.NORTH);
        add(divisionHorizontal, BorderLayout.CENTER);
        add(barraEstado(), BorderLayout.SOUTH);
    }

    private void configurarTablas() {
        Font fuente = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        for (JTable tabla : new JTable[] {tablaTokens, tablaErrores, tablaSimbolos, tablaEstadisticas}) {
            tabla.setFont(fuente);
            tabla.setRowHeight(22);
            tabla.getTableHeader().setReorderingAllowed(false);
        }
        ajustarColumnas(tablaTokens, 50, 220, 160, 60, 70);
        ajustarColumnas(tablaErrores, 50, 110, 560, 60, 70);
        ajustarColumnas(tablaSimbolos, 200, 200, 160, 70);
        ajustarColumnas(tablaEstadisticas, 260, 100);
        saltarAlHacerDobleClic(tablaTokens, 3, 4);
        saltarAlHacerDobleClic(tablaErrores, 3, 4);
        saltarAlHacerDobleClic(tablaSimbolos, 3, -1);
    }

    private JToolBar barraHerramientas() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(boton("Guardar", "Guardar (Ctrl+S)", this::guardarArchivo));
        barra.addSeparator();
        barra.add(boton("Compilar", "Compilar (F5)", this::analizarCodigo));
        barra.add(boton("Guardar promptzal", "Guardar el promptzal generado", this::guardarPrompt));
        barra.addSeparator();
        barra.add(boton("Reportes", "Generar los reportes", this::generarReportes));
        return barra;
    }

    private JTabbedPane panelExplorador() {
        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 5));
        botones.add(boton("Nuevo Archivo", "Nuevo (Ctrl+N)", this::nuevaPestana));
        botones.add(boton("Abrir Archivo", "Abrir (Ctrl+O)", this::abrirArchivo));
        botones.add(boton("Generar AFD", "Genera y abre la imagen del AFD", this::verAFD));
        botones.add(boton("Cerrar Todo", "Cierra todas las pestañas", this::cerrarTodo));
        botones.add(boton("Salir", "Cerrar el programa", this::salir));

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contenedor.add(botones, BorderLayout.NORTH);

        JTabbedPane explorador = new JTabbedPane();
        explorador.addTab("Explorador", contenedor);
        explorador.setMinimumSize(new Dimension(150, 0));
        return explorador;
    }

    private JPanel barraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEtchedBorder());
        barra.add(etiquetaEstado, BorderLayout.WEST);
        barra.add(etiquetaCursor, BorderLayout.EAST);
        return barra;
    }

    private JButton boton(String texto, String ayuda, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFocusable(false);
        boton.setToolTipText(ayuda);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void registrarAtajos() {
        int control = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_S, control), "guardar", this::guardarArchivo);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_O, control), "abrir", this::abrirArchivo);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_N, control), "nuevo", this::nuevaPestana);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "compilar", this::analizarCodigo);
    }

    private void atajo(KeyStroke tecla, String nombre, Runnable accion) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(tecla, nombre);
        getRootPane().getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.run();
            }
        });
    }

    private void nuevaPestana() {
        agregarPestana(null, "");
    }

    private void agregarPestana(Path ruta, String contenido) {
        PestanaEditor pestana = new PestanaEditor(contenido, ruta, fondo, compilador::tokenizar);
        JLabel titulo = new JLabel();
        titulos.put(pestana, titulo);
        pestana.setAlCambiarEstado(() -> actualizarTitulo(pestana));
        pestana.setAlEditarTexto(() -> pintarPestana(pestana, null));
        pestana.addCaretListener(e -> {
            if (pestana == getPestanaActual()) actualizarCursor(pestana);
        });

        pestanasEditor.addTab(pestana.getNombre(), pestana);
        int indice = pestanasEditor.indexOfComponent(pestana);
        pestanasEditor.setTabComponentAt(indice, panelTitulo(titulo, botonCerrar(pestana)));
        actualizarTitulo(pestana);
        pestanasEditor.setSelectedIndex(indice);
        SwingUtilities.invokeLater(() -> pestana.getEditor().requestFocusInWindow());
    }

    private JPanel panelTitulo(JLabel titulo, JButton cerrar) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.add(titulo);
        panel.add(cerrar);
        panel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 2));
        return panel;
    }

    private JButton botonCerrar(PestanaEditor pestana) {
        JButton cerrar = new JButton("\u00d7");
        cerrar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        cerrar.setMargin(new Insets(0, 4, 0, 4));
        cerrar.setFocusable(false);
        cerrar.setBorderPainted(false);
        cerrar.setContentAreaFilled(false);
        cerrar.setForeground(Color.GRAY);
        cerrar.setToolTipText("Cerrar pestaña");
        cerrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cerrar.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cerrar.setForeground(Color.GRAY);
            }
        });
        cerrar.addActionListener(e -> cerrarPestana(pestana));
        return cerrar;
    }

    private void cerrarPestana(PestanaEditor pestana) {
        if (pestana.isModificado() && !resolverCambios(pestana)) return;
        titulos.remove(pestana);
        pestanasEditor.remove(pestana);
        if (pestana == pestanaCompilada) pestanaCompilada = null;
        if (pestanasEditor.getTabCount() == 0) nuevaPestana();
    }

    private void cerrarTodo() {
        if (!resolverTodosLosCambios()) return;
        pestanasEditor.removeAll();
        titulos.clear();
        limpiarResultados();
        nuevaPestana();
    }

    private void salir() {
        if (!resolverTodosLosCambios()) return;
        dispose();
        System.exit(0);
    }

    private boolean resolverTodosLosCambios() {
        for (int i = 0; i < pestanasEditor.getTabCount(); i++) {
            PestanaEditor pestana = (PestanaEditor) pestanasEditor.getComponentAt(i);
            if (pestana.isModificado() && !resolverCambios(pestana)) return false;
        }
        return true;
    }

    private boolean resolverCambios(PestanaEditor pestana) {
        pestanasEditor.setSelectedComponent(pestana);
        String[] opciones = {"Guardar", "Descartar", "Cancelar"};
        int opcion = JOptionPane.showOptionDialog(this,
                "'" + pestana.getNombre() + "' tiene cambios sin guardar.",
                "Cambios sin guardar", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, opciones, opciones[0]);
        if (opcion == 0) return guardar(pestana);
        return opcion == 1;
    }

    private PestanaEditor getPestanaActual() {
        return (PestanaEditor) pestanasEditor.getSelectedComponent();
    }

    private PestanaEditor buscarPestana(Path ruta) {
        for (int i = 0; i < pestanasEditor.getTabCount(); i++) {
            PestanaEditor pestana = (PestanaEditor) pestanasEditor.getComponentAt(i);
            if (ruta.equals(pestana.getRuta())) return pestana;
        }
        return null;
    }

    private void actualizarTitulo(PestanaEditor pestana) {
        JLabel etiqueta = titulos.get(pestana);
        if (etiqueta == null) return;
        etiqueta.setText((pestana.isModificado() ? "* " : "") + pestana.getNombre() + "  ");
        int indice = pestanasEditor.indexOfComponent(pestana);
        if (indice >= 0) {
            pestanasEditor.setToolTipTextAt(indice,
                    pestana.getRuta() == null ? "Sin guardar" : pestana.getRuta().toString());
        }
        if (pestana == getPestanaActual()) actualizarTituloVentana();
    }
    
    private void pintarPestana(PestanaEditor pestana, Color color) {
        JLabel etiqueta = titulos.get(pestana);
        if (etiqueta == null || !(etiqueta.getParent() instanceof JComponent panel)) return;
        if (color == null && !panel.isOpaque()) return;
        panel.setOpaque(color != null);
        if (color != null) panel.setBackground(color);
        panel.repaint();
    }

    private void actualizarTituloVentana() {
        PestanaEditor actual = getPestanaActual();
        setTitle(actual == null ? TITULO_VENTANA : TITULO_VENTANA + " - " + actual.getNombre());
    }

    private void alCambiarPestana() {
        actualizarCursor(getPestanaActual());
        actualizarTituloVentana();
    }

    private void actualizarCursor(PestanaEditor pestana) {
        etiquetaCursor.setText(pestana == null ? "1 : 1 " : pestana.posicionCursor());
    }

    private void abrirArchivo() {
        JFileChooser chooser = selector();
        chooser.setFileFilter(FILTRO_PZ);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);
        Path ruta = chooser.getSelectedFile().toPath();

        PestanaEditor abierta = buscarPestana(ruta);
        if (abierta != null) {
            pestanasEditor.setSelectedComponent(abierta);
            return;
        }
        try {
            String contenido = Files.readString(ruta).replace("\r\n", "\n");
            agregarPestana(ruta, contenido);
            etiquetaEstado.setText(" Archivo cargado: " + ruta.getFileName());
        } catch (IOException ex) {
            mensaje("No se pudo abrir el archivo: " + ex.getMessage());
        }
    }

    private void guardarArchivo() {
        guardar(getPestanaActual());
    }

    private boolean guardar(PestanaEditor pestana) {
        if (pestana == null) {
            mensaje("No hay ningún archivo abierto para guardar.");
            return false;
        }
        if (pestana.getTexto().isBlank() && JOptionPane.showConfirmDialog(this,
                "El editor está vacío. ¿Seguro que desea guardar un archivo en blanco?",
                "Aviso de documento vacío", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return false;
        }
        Path ruta = pestana.getRuta();
        if (ruta == null) {
            JFileChooser chooser = selector();
            chooser.setFileFilter(FILTRO_PZ);
            chooser.setSelectedFile(new File("programa.pz"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return false;
            recordarDirectorio(chooser);
            ruta = conExtension(chooser.getSelectedFile().toPath(), ".pz");
            if (!confirmarSobrescribir(ruta)) return false;
        }
        if (!escribir(ruta, pestana.getTexto())) return false;
        pestana.marcarGuardado(ruta);
        etiquetaEstado.setText(" Guardado: " + ruta.getFileName());
        return true;
    }

    private void guardarPrompt() {
        if (ultimo == null) {
            mensaje("Compila un programa primero.");
            return;
        }
        FileNameExtensionFilter markdown = new FileNameExtensionFilter("Markdown (*.md)", "md");
        FileNameExtensionFilter texto = new FileNameExtensionFilter("Texto (*.txt)", "txt");
        JFileChooser chooser = selector();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(markdown);
        chooser.addChoosableFileFilter(texto);
        chooser.setFileFilter(markdown);
        chooser.setSelectedFile(new File("promptzal"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);

        boolean comoTexto = chooser.getFileFilter() == texto;
        Path ruta = conExtension(chooser.getSelectedFile().toPath(),
                comoTexto ? ".txt" : ".md", comoTexto ? ".md" : ".txt");
        if (confirmarSobrescribir(ruta) && escribir(ruta, ultimo.getPromptzal())) {
            etiquetaEstado.setText(" Promptzal guardado: " + ruta.getFileName());
        }
    }

    private boolean confirmarSobrescribir(Path ruta) {
        if (!Files.exists(ruta)) return true;
        return JOptionPane.showConfirmDialog(this,
                "'" + ruta.getFileName() + "' ya existe. ¿Desea reemplazarlo?",
                "Confirmar reemplazo", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private JFileChooser selector() {
        return new JFileChooser(ultimoDirectorio);
    }

    private void recordarDirectorio(JFileChooser chooser) {
        ultimoDirectorio = chooser.getCurrentDirectory();
    }

    private void analizarCodigo() {
        PestanaEditor pestana = getPestanaActual();
        if (pestana == null) {
            mensaje("No hay ningún archivo abierto para compilar.");
            return;
        }
        long inicio = System.nanoTime();
        ultimo = compilador.compilar(pestana.getTexto());
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;
        pestanaCompilada = pestana;

        panelPrompt.setText(ultimo.getPromptzal());
        panelPrompt.setCaretPosition(0);
        mostrarErrores(ultimo.getErrores());
        mostrarTokens(ultimo.getTokens());
        mostrarSimbolos(ultimo);
        mostrarEstadisticas(ultimo, pestana);
        generarImagenAst(ultimo);

        int errores = ultimo.getErrores().size();
        pintarPestana(pestana, errores == 0 ? COLOR_EXITO : COLOR_ERROR);
        if (errores == 0) {
            etiquetaEstado.setText(" Compilación exitosa (" + milisegundos + " ms)");
            pestanasInferior.setSelectedComponent(scrollPrompt);
        } else {
            etiquetaEstado.setText(" Compilación terminada con " + errores + (errores == 1 ? " error" : " errores"));
            pestanasInferior.setSelectedComponent(scrollErrores);
        }
    }

    private void mostrarErrores(List<ErrorCompilacion> errores) {
        modeloErrores.setRowCount(0);
        int numero = 1;
        for (ErrorCompilacion e : errores) {
            modeloErrores.addRow(new Object[] {numero++, nombreTipo(e.getTipo()), e.getDescripcion(),
                e.getFila(), e.getColumna()});
        }
        rotular(scrollErrores, "Errores (" + errores.size() + ")");
    }

    private void mostrarTokens(List<Token> tokens) {
        modeloTokens.setRowCount(0);
        int numero = 1;
        for (Token t : tokens) {
            modeloTokens.addRow(new Object[] {numero++, t.getLexema(), t.getTipo(), t.getFila(), t.getColumna()});
        }
        rotular(scrollTokens, "Tokens (" + tokens.size() + ")");
    }

    private void mostrarSimbolos(ResultadoCompilacion resultado) {
        modeloSimbolos.setRowCount(0);
        List<Simbolo> simbolos = resultado.getTabla().getSimbolos();
        for (Simbolo s : simbolos) {
            modeloSimbolos.addRow(new Object[] {s.getNombre(), s.getTipo().getEtiqueta(), s.getAmbito(), s.getFila()});
        }
        rotular(scrollSimbolos, "Símbolos (" + simbolos.size() + ")");
    }

    private void mostrarEstadisticas(ResultadoCompilacion resultado, PestanaEditor pestana) {
        modeloEstadisticas.setRowCount(0);
        modeloEstadisticas.addRow(new Object[] {"Total de tokens", resultado.getTokens().size()});
        modeloEstadisticas.addRow(new Object[] {"Total de líneas", pestana.getTotalLineas()});
        modeloEstadisticas.addRow(new Object[] {"Total de errores", resultado.getErrores().size()});
        Map<String, Integer> frecuencia = new LinkedHashMap<>();
        for (Token t : resultado.getTokens()) {
            frecuencia.merge(t.getTipo().toString(), 1, Integer::sum);
        }
        frecuencia.forEach((tipo, cantidad) -> modeloEstadisticas.addRow(new Object[] {tipo, cantidad}));
    }

    private void generarImagenAst(ResultadoCompilacion resultado) {
        int generacion = ++generacionAst;
        etiquetaAst.setIcon(null);
        etiquetaAst.setText("Generando imagen del AST...");
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                Path dot = Files.createTempFile("promptzal_ast_", ".dot");
                Path png = Files.createTempFile("promptzal_ast_", ".png");
                try {
                    new MostrarGraphviz().compilarImagen(
                            new GeneradorDotAst().generarCodigo(resultado.getPrograma()), dot, png);
                    return ImageIO.read(png.toFile());
                } finally {
                    Files.deleteIfExists(dot);
                    Files.deleteIfExists(png);
                }
            }

            @Override
            protected void done() {
                if (generacion != generacionAst) return;
                try {
                    BufferedImage imagen = get();
                    if (imagen == null) {
                        etiquetaAst.setText("No se pudo leer la imagen generada.");
                        return;
                    }
                    etiquetaAst.setText(null);
                    etiquetaAst.setIcon(new ImageIcon(imagen));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    etiquetaAst.setIcon(null);
                    etiquetaAst.setText("No se pudo generar la imagen del AST (¿Graphviz instalado?): "
                            + mensajeDe(ex.getCause()));
                }
            }
        }.execute();
    }

    private void limpiarResultados() {
        panelPrompt.setText("");
        for (DefaultTableModel modelo : new DefaultTableModel[] {modeloTokens, modeloErrores,
            modeloSimbolos, modeloEstadisticas}) {
            modelo.setRowCount(0);
        }
        rotular(scrollTokens, "Tokens");
        rotular(scrollErrores, "Errores");
        rotular(scrollSimbolos, "Símbolos");
        generacionAst++;
        etiquetaAst.setIcon(null);
        etiquetaAst.setText(TEXTO_AST_VACIO);
        ultimo = null;
        pestanaCompilada = null;
        etiquetaEstado.setText(" Listo");
    }

    private void rotular(JScrollPane panel, String texto) {
        pestanasInferior.setTitleAt(pestanasInferior.indexOfComponent(panel), texto);
    }

    private void ajustarColumnas(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    private void saltarAlHacerDobleClic(JTable tabla, int columnaFila, int columnaColumna) {
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (e.getClickCount() != 2 || fila < 0 || pestanaCompilada == null
                        || pestanasEditor.indexOfComponent(pestanaCompilada) < 0) {
                    return;
                }
                int linea = (Integer) tabla.getValueAt(fila, columnaFila);
                int columna = columnaColumna < 0 ? 1 : (Integer) tabla.getValueAt(fila, columnaColumna);
                pestanasEditor.setSelectedComponent(pestanaCompilada);
                pestanaCompilada.irA(linea, columna);
            }
        });
    }

    private String nombreTipo(ErrorCompilacion.Tipo tipo) {
        return switch (tipo) {
            case LEXICO -> "Léxico";
            case SINTACTICO -> "Sintáctico";
            case SEMANTICO -> "Semántico";
        };
    }

    private void generarReportes() {
        if (ultimo == null) {
            JOptionPane.showMessageDialog(this, "Primero debe compilar el código antes de generar reportes.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = selector();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, "Elegir carpeta") != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);
        Path carpeta = chooser.getSelectedFile().toPath();

        boolean ok = escribir(carpeta.resolve("reporte_errores.html"), new ReporteErrores().generar(ultimo.getErrores()));
        ok &= escribir(carpeta.resolve("reporte_simbolos.html"), new ReporteSimbolos().generar(ultimo.getTabla()));
        ok &= escribir(carpeta.resolve("reporte_ast.html"), new ReporteAst().generar(ultimo.getPrograma()));
        if (!ok) return;

        boolean imagen = guardarImagenAst(carpeta);
        etiquetaEstado.setText(" Reportes generados en: " + carpeta);
        mensaje("Reportes generados en: " + carpeta
                + (imagen ? "" : "\n(No se generó la imagen del AST porque Graphviz no está disponible.)"));
    }

    private boolean guardarImagenAst(Path carpeta) {
        try {
            new MostrarGraphviz().compilarImagen(new GeneradorDotAst().generarCodigo(ultimo.getPrograma()),
                    carpeta.resolve("reporte_ast.dot"), carpeta.resolve("reporte_ast.png"));
            return true;
        } catch (IOException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void verAFD() {
        Path imagen = Path.of("reportes/afd.png");
        try {
            new MostrarGraphviz().compilarImagen(new GeneradorDot(new TablaTransiciones()).generarDot(),
                    Path.of("reportes/afd.dot"), imagen);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(imagen.toFile());
            }
            etiquetaEstado.setText(" Imagen del AFD generada en " + imagen.toAbsolutePath());
        } catch (IOException ex) {
            mensaje("No se pudo generar o abrir el AFD:\n" + ex.getMessage());
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

    private String mensajeDe(Throwable error) {
        return error.getMessage() == null ? error.toString() : error.getMessage();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // se conserva el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> {
            try {
                Class<?> bienvenida = Class.forName(VentanaPrincipal.class.getPackageName() + ".VentanaBienvenida");
                Object ventana = bienvenida.getDeclaredConstructor().newInstance();
                bienvenida.getMethod("setVisible", boolean.class).invoke(ventana, true);
            } catch (ReflectiveOperationException ex) {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }
}