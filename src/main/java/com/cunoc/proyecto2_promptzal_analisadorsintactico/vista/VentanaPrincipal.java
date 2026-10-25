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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.Box;
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
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.tree.TreePath;

public class VentanaPrincipal extends JFrame {

    static {
        Tema.aplicar();
    }

    private static final String TITULO_VENTANA = "PromptZal IDE";
    private static final String TEXTO_AST_VACIO = "Compila este archivo para ver su AST";
    private static final FileNameExtensionFilter FILTRO_PZ =
            new FileNameExtensionFilter("Archivos PromptZal (*.pz)", "pz");
    private static final String EJEMPLO_REFERENCIA = """
            @modelo "claude-sonnet-4-6"
            @rol "analista de datos"
            @formato "markdown"

            // Agente que prepara el analisis de un archivo de ventas
            AGENTE analista {
                contexto = "Eres un analista de datos experto"
                variable ventas = CARGAR ("ventas.csv")
                PREGUNTAR "Cuales son las 3 tendencias principales?" SOBRE ventas -> tendencias
                RESUMIR tendencias EN 100 palabras -> resumen
            }

            EJECUTAR analista
            EXPORTAR resumen
            """;

    private static final class Salida {
        private final ResultadoCompilacion resultado;
        private final int totalLineas;
        private BufferedImage imagenAst;
        private Path rutaImagenAst;
        private double zoomAst = 1.0;
        private String mensajeAst = "Generando imagen del AST...";

        private Salida(ResultadoCompilacion resultado, int totalLineas) {
            this.resultado = resultado;
            this.totalLineas = totalLineas;
        }
    }
    //Atributos
    private final Compilador compilador = new Compilador();
    private final Image fondo;
    private final Map<PestanaEditor, JLabel> titulos = new HashMap<>();
    private final Map<PestanaEditor, Salida> salidas = new HashMap<>();

    private final JTabbedPane pestanasEditor = new JTabbedPane();
    private final PanelPestanas pestanasInferior = new PanelPestanas();
    private final JLabel etiquetaEstado = new JLabel(" Listo");
    private final JLabel etiquetaCursor = new JLabel("1 : 1 ");

    private final JTextArea panelPrompt = new JTextArea();
    private final JLabel etiquetaAst = new JLabel(TEXTO_AST_VACIO, JLabel.CENTER);

    private final DefaultTableModel modeloTokens = crearModelo("No.", "Lexema", "Tipo", "Fila", "Columna");
    private final DefaultTableModel modeloErrores = crearModelo("No.", "Tipo", "Descripción", "Fila", "Columna");
    private final DefaultTableModel modeloSimbolos = crearModelo("Nombre", "Tipo", "Ámbito", "Línea");
    private final DefaultTableModel modeloEstadisticas = crearModelo("Métrica", "Valor");

    private final JTable tablaTokens = new TablaOscura(modeloTokens);
    private final JTable tablaErrores = new TablaOscura(modeloErrores);
    private final JTable tablaSimbolos = new TablaOscura(modeloSimbolos);
    private final JTable tablaEstadisticas = new TablaOscura(modeloEstadisticas);

    private final JScrollPane scrollPrompt = new JScrollPane(panelPrompt);
    private final JScrollPane scrollTokens = new JScrollPane(tablaTokens);
    private final JScrollPane scrollErrores = new JScrollPane(tablaErrores);
    private final JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
    private final JScrollPane scrollEstadisticas = new JScrollPane(tablaEstadisticas);
    private final JScrollPane scrollAst = new JScrollPane(etiquetaAst);

    private final DefaultMutableTreeNode raizEsquema = new DefaultMutableTreeNode("Programa");
    private final DefaultTreeModel modeloEsquema = new DefaultTreeModel(raizEsquema);
    private final JTree arbolEsquema = new JTree(modeloEsquema);
    private final JScrollPane scrollEsquema = new JScrollPane(arbolEsquema);
    
    
    private final JPanel contenedorBotonAst = new JPanel(new BorderLayout());
    private JButton botonAstActual;
    private final JPanel contenedorBotonGuardarAst = new JPanel(new BorderLayout());
    private JButton botonGuardarAstActual;
    
    private File ultimoDirectorio;

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
        configurarEsquema();
        configurarZoomAst();
        panelPrompt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        panelPrompt.setEditable(false);
        panelPrompt.setLineWrap(true);
        panelPrompt.setWrapStyleWord(true);
        panelPrompt.setMargin(new Insets(6, 8, 6, 8));

        pestanasEditor.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        pestanasEditor.addChangeListener(e -> alCambiarPestana());

        pestanasInferior.agregar("Promptzal", Tema.VIOLETA, scrollPrompt);
        pestanasInferior.agregar("Tokens", Tema.CIAN, scrollTokens);
        pestanasInferior.agregar("Errores", Tema.MAGENTA, scrollErrores);
        pestanasInferior.agregar("Símbolos", Tema.VERDE, scrollSimbolos);
        pestanasInferior.agregar("Estadísticas", Tema.AMBAR, scrollEstadisticas);
        pestanasInferior.agregar("AST", Tema.LIMA, scrollAst);

        JSplitPane divisionVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestanasEditor, pestanasInferior);
        divisionVertical.setResizeWeight(0.7);
        divisionVertical.setContinuousLayout(true);

        JSplitPane divisionHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelExplorador(), divisionVertical);
        divisionHorizontal.setDividerLocation(180);
        divisionHorizontal.setContinuousLayout(true);

        add(barraHerramientas(), BorderLayout.NORTH);
        add(divisionHorizontal, BorderLayout.CENTER);
        add(barraEstado(), BorderLayout.SOUTH);
        aplicarColores();
    }

    private void configurarTablas() {
        Font fuente = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        for (JTable tabla : new JTable[] {tablaTokens, tablaErrores, tablaSimbolos, tablaEstadisticas}) {
            tabla.setFont(fuente);
            tabla.setRowHeight(22);
            tabla.setFillsViewportHeight(true);
            tabla.getTableHeader().setReorderingAllowed(false);
        }
        ajustarColumnas(tablaTokens, 50, 220, 160, 60, 70);
        ajustarColumnas(tablaErrores, 50, 110, 560, 60, 70);
        ajustarColumnas(tablaSimbolos, 200, 200, 160, 70);
        ajustarColumnas(tablaEstadisticas, 260, 100);
        saltarAlHacerDobleClic(tablaTokens, 3, 4);
        saltarAlHacerDobleClic(tablaErrores, 3, 4);
        saltarAlHacerDobleClic(tablaSimbolos, 3, -1);

        DefaultTableCellRenderer renderTokens = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object tipo = table.getValueAt(row, 2);
                    c.setForeground(colorPorToken(tipo == null ? "" : tipo.toString()));
                }
                return c;
            }
        };
        tablaTokens.getColumnModel().getColumn(1).setCellRenderer(renderTokens);
        tablaTokens.getColumnModel().getColumn(2).setCellRenderer(renderTokens);

        tablaErrores.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    c.setForeground(switch (value.toString()) {
                        case "Léxico" -> Tema.AMBAR;
                        case "Sintáctico" -> Tema.MAGENTA;
                        case "Semántico" -> Tema.VIOLETA;
                        default -> Tema.TEXTO;
                    });
                }
                return c;
            }
        });

        DefaultTableCellRenderer renderEstadisticas = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object metricaObj = table.getValueAt(row, 0);
                    String metrica = metricaObj == null ? "" : metricaObj.toString();
                    c.setForeground(switch (metrica) {
                        case "Total de tokens" -> Tema.VERDE;
                        case "Total de líneas" -> Tema.CIAN;
                        case "Total de errores" -> Tema.PESTANA_ERROR;
                        default -> colorPorToken(metrica);
                    });
                }
                return c;
            }
        };
        tablaEstadisticas.getColumnModel().getColumn(0).setCellRenderer(renderEstadisticas);
        tablaEstadisticas.getColumnModel().getColumn(1).setCellRenderer(renderEstadisticas);
    }

    private Color colorPorToken(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "DIRECTIVA" -> Tema.VIOLETA;
            case "AGENTE", "CONTEXTO", "VARIABLE", "EJECUTAR", "EXPORTAR", "PALABRA_RESERVADA" -> Tema.CIAN;
            case "COMANDO_IA", "COMANDO" -> Tema.MAGENTA;
            case "CARGAR", "FUNCION" -> Tema.LIMA;
            case "CONECTOR", "FLECHA" -> Tema.AMBAR;
            case "CADENA" -> Tema.VERDE;
            case "NUMERO" -> Tema.AMBAR;
            case "ID", "IDENTIFICADOR" -> Tema.TEXTO;
            case "IGUAL", "MAS", "LLAVE_A", "LLAVE_C", "PAR_A", "PAR_C", "COMA" -> Tema.TENUE;
            default -> Tema.TEXTO;
        };
    }

    private void configurarEsquema() {
        arbolEsquema.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        arbolEsquema.setRowHeight(20);
        DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
        render.setBackgroundNonSelectionColor(Tema.FONDO);
        render.setBackgroundSelectionColor(Tema.SELECCION);
        render.setTextNonSelectionColor(Tema.TEXTO);
        render.setTextSelectionColor(Color.WHITE);
        arbolEsquema.setCellRenderer(render);
        scrollEsquema.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));
        
                arbolEsquema.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PestanaEditor pestana = getPestanaActual();
                TreePath ruta = arbolEsquema.getPathForLocation(e.getX(), e.getY());
                if (e.getClickCount() != 2 || ruta == null || pestana == null) return;
                String texto = ruta.getLastPathComponent().toString();
                int inicio = texto.lastIndexOf("(L");
                if (inicio < 0) return;
                try {
                    pestana.irA(Integer.parseInt(texto.substring(inicio + 2, texto.length() - 1)), 1);
                } catch (NumberFormatException ex) {
                    // nodo de grupo, sin número de línea
                }
            }
        });
    }

    private void configurarZoomAst() {
        scrollAst.addMouseWheelListener(e -> {
            if (!e.isControlDown()) return;
            Salida salida = salidas.get(getPestanaActual());
            if (salida == null || salida.imagenAst == null) return;
            e.consume();
            double factor = e.getWheelRotation() < 0 ? 1.15 : 0.85;
            salida.zoomAst = Math.max(0.2, Math.min(3.0, salida.zoomAst * factor));
            mostrarAst(salida);
            etiquetaEstado.setText(" Zoom AST: " + Math.round(salida.zoomAst * 100) + "% (Ctrl + Rueda)");
        });
    }

    private void aplicarColores() {
        for (JTable tabla : new JTable[] {tablaTokens, tablaErrores, tablaSimbolos, tablaEstadisticas}) {
            tabla.setBackground(Tema.FONDO);
            tabla.setForeground(Tema.TEXTO);
            tabla.setGridColor(Tema.BORDE);
            tabla.setSelectionBackground(Tema.SELECCION);
            tabla.setSelectionForeground(Color.WHITE);
            tabla.getTableHeader().setBackground(Tema.PANEL_CLARO);
            tabla.getTableHeader().setForeground(Tema.CIAN);
        }
        panelPrompt.setBackground(Tema.FONDO);
        panelPrompt.setForeground(Tema.TEXTO);
        panelPrompt.setCaretColor(Tema.CIAN);
        etiquetaAst.setOpaque(true);
        etiquetaAst.setBackground(Tema.FONDO);
        etiquetaAst.setForeground(Tema.TENUE);
        arbolEsquema.setBackground(Tema.FONDO);
        for (JScrollPane scroll : new JScrollPane[] {scrollPrompt, scrollTokens, scrollErrores,
            scrollSimbolos, scrollEstadisticas, scrollAst, scrollEsquema}) {
            scroll.getViewport().setBackground(Tema.FONDO);
        }
    }

    private JPanel barraHerramientas() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        barra.setBackground(Tema.PANEL_CLARO);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE));
        barra.add(boton("Guardar", "Guardar (Ctrl+S)", Tema.CIAN, this::guardarArchivo));
        barra.add(Box.createHorizontalStrut(12));
        barra.add(boton("Compilar", "Compilar (F5)", Tema.VERDE, this::analizarCodigo));
        barra.add(boton("Copiar promptzal", "Copiar el promptzal al portapapeles", Tema.LIMA, this::copiarPrompt));
        barra.add(boton("Guardar promptzal", "Guardar el promptzal generado", Tema.VIOLETA, this::guardarPrompt));
        barra.add(Box.createHorizontalStrut(12));
        barra.add(boton("Reportes", "Generar los reportes", Tema.AMBAR, this::generarReportes));
        barra.add(boton("Buscar", "Buscar en el editor (Ctrl+F)", Tema.CIAN, this::buscarEnEditor));
        return barra;
    }

    private JTabbedPane panelExplorador() {
        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 6));
        botones.setOpaque(false);
        contenedorBotonAst.setOpaque(false);
        contenedorBotonGuardarAst.setOpaque(false);
        actualizarBotonAst(null);

        botones.add(boton("Nuevo Archivo", "Nuevo (Ctrl+N)", Tema.CIAN, this::nuevaPestana));
        botones.add(boton("Abrir Archivo", "Abrir (Ctrl+O)", Tema.CIAN, this::abrirArchivo));
        botones.add(boton("Cargar Ejemplo", "Carga el programa de referencia", Tema.LIMA, this::cargarEjemplo));
        botones.add(boton("Generar AFD", "Genera y abre la imagen del AFD", Tema.VIOLETA, this::verAFD));
        botones.add(contenedorBotonAst);
        botones.add(contenedorBotonGuardarAst);
        botones.add(boton("Cerrar Todo", "Cierra todas las pestañas", Tema.AMBAR, this::cerrarTodo));
        botones.add(boton("Salir", "Cerrar el programa", Tema.MAGENTA, this::salir));

        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setBackground(Tema.PANEL);
        contenedor.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contenedor.add(botones, BorderLayout.NORTH);
        contenedor.add(scrollEsquema, BorderLayout.CENTER);

        JTabbedPane explorador = new JTabbedPane();
        explorador.addTab("Explorador", contenedor);
        explorador.setMinimumSize(new Dimension(150, 0));
        return explorador;
    }

    private JPanel barraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Tema.PANEL_CLARO);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));
        etiquetaEstado.setForeground(Tema.TEXTO);
        etiquetaCursor.setForeground(Tema.TENUE);
        barra.add(etiquetaEstado, BorderLayout.WEST);
        barra.add(etiquetaCursor, BorderLayout.EAST);
        return barra;
    }

    private JButton boton(String texto, String ayuda, Color acento, Runnable accion) {
        JButton boton = new BotonNeon(texto, acento);
        boton.setToolTipText(ayuda);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void registrarAtajos() {
        int control = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_S, control), "guardar", this::guardarArchivo);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_O, control), "abrir", this::abrirArchivo);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_N, control), "nuevo", this::nuevaPestana);
        atajo(KeyStroke.getKeyStroke(KeyEvent.VK_F, control), "buscar", this::buscarEnEditor);
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

    private void cargarEjemplo() {
        agregarPestana(null, EJEMPLO_REFERENCIA);
        etiquetaEstado.setText(" Ejemplo de referencia cargado");
    }

    private void buscarEnEditor() {
        PestanaEditor pestana = getPestanaActual();
        if (pestana == null || !(pestana.getEditor() instanceof JTextComponent editor)) return;
        String termino = JOptionPane.showInputDialog(this, "Texto a buscar:", "Buscar en el archivo",
                JOptionPane.PLAIN_MESSAGE);
        if (termino == null || termino.isEmpty()) return;
        String texto = editor.getText();
        int desde = editor.getSelectionEnd();
        int pos = texto.indexOf(termino, desde);
        if (pos < 0 && desde > 0) {
            pos = texto.indexOf(termino);
        }
        if (pos >= 0) {
            editor.requestFocusInWindow();
            editor.select(pos, pos + termino.length());
            etiquetaEstado.setText(" Coincidencia encontrada: '" + termino + "'");
        } else {
            etiquetaEstado.setText(" No se encontró: '" + termino + "'");
            mensaje("No se encontró '" + termino + "' en el archivo actual.");
        }
    }

    private void agregarPestana(Path ruta, String contenido) {
        PestanaEditor pestana = new PestanaEditor(contenido, ruta, fondo, compilador::tokenizar);
        JLabel titulo = new JLabel();
        titulos.put(pestana, titulo);
        pestana.setAlCambiarEstado(() -> actualizarTitulo(pestana));
        pestana.setAlEditarTexto(() -> {
            pintarPestana(pestana, null);
            MarcadorErrores.limpiar(pestana);
        });
        
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
        panel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 2));
        panel.add(titulo);
        panel.add(cerrar);
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
        salidas.remove(pestana);
        pestanasEditor.remove(pestana);
        if (pestanasEditor.getTabCount() == 0) nuevaPestana();
    }

    private void cerrarTodo() {
        if (!resolverTodosLosCambios()) return;
        titulos.clear();
        salidas.clear();
        pestanasEditor.removeAll();
        nuevaPestana();
        etiquetaEstado.setText(" Listo");
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
        etiqueta.setForeground(color == null ? null : Color.WHITE);
        panel.repaint();
    }

    private void actualizarTituloVentana() {
        PestanaEditor actual = getPestanaActual();
        setTitle(actual == null ? TITULO_VENTANA : TITULO_VENTANA + " - " + actual.getNombre());
    }

    private void alCambiarPestana() {
        PestanaEditor actual = getPestanaActual();
        actualizarCursor(actual);
        actualizarTituloVentana();
        mostrarSalida(salidas.get(actual));
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

    private void copiarPrompt() {
        Salida salida = salidas.get(getPestanaActual());
        if (salida == null || panelPrompt.getText().isBlank()) {
            mensaje("Compila este archivo primero para copiar el promptzal.");
            return;
        }
        StringSelection seleccion = new StringSelection(panelPrompt.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(seleccion, null);
        etiquetaEstado.setText(" Promptzal copiado al portapapeles");
    }

        private void guardarPrompt() {
        PestanaEditor actual = getPestanaActual();
        Salida salida = salidas.get(actual);
        if (salida == null) {
            mensaje("Compila este archivo primero.");
            return;
        }
        String sufijo = sufijoUnico(actual);
        FileNameExtensionFilter markdown = new FileNameExtensionFilter("Markdown (*.md)", "md");
        FileNameExtensionFilter texto = new FileNameExtensionFilter("Texto (*.txt)", "txt");
        JFileChooser chooser = selector();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(markdown);
        chooser.addChoosableFileFilter(texto);
        chooser.setFileFilter(markdown);
        chooser.setSelectedFile(new File("promptzal_" + sufijo));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);

        boolean comoTexto = chooser.getFileFilter() == texto;
        Path ruta = conExtension(chooser.getSelectedFile().toPath(),
                comoTexto ? ".txt" : ".md", comoTexto ? ".md" : ".txt");
        ruta = asegurarNoSobrescribir(ruta);
        if (escribir(ruta, salida.resultado.getPromptzal())) {
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
        ResultadoCompilacion resultado = compilador.compilar(pestana.getTexto());
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

        Salida salida = new Salida(resultado, pestana.getTotalLineas());
        salidas.put(pestana, salida);
        mostrarSalida(salida);
        generarImagenAst(salida);

        int errores = resultado.getErrores().size();
        pintarPestana(pestana, errores == 0 ? Tema.PESTANA_OK : Tema.PESTANA_ERROR);
        if (errores == 0) {
            etiquetaEstado.setText(" Compilación exitosa (" + milisegundos + " ms)");
            pestanasInferior.seleccionar(scrollPrompt);            
        } else {
            etiquetaEstado.setText(" Compilación terminada con " + errores + (errores == 1 ? " error" : " errores"));
            pestanasInferior.seleccionar(scrollErrores);
        }
    }

    private void mostrarSalida(Salida salida) {
        PestanaEditor actual = getPestanaActual();
        actualizarBotonAst(salida);
        if (salida == null) {
            MarcadorErrores.limpiar(actual);
            limpiarPaneles();
            return;
        }
        ResultadoCompilacion resultado = salida.resultado;
        MarcadorErrores.marcar(actual, resultado.getErrores());
        panelPrompt.setText(resultado.getPromptzal());
        panelPrompt.setCaretPosition(0);
        mostrarErrores(resultado.getErrores());
        mostrarTokens(resultado.getTokens());
        mostrarSimbolos(resultado);
        mostrarEstadisticas(resultado, salida.totalLineas);
        mostrarEsquema(resultado);
        mostrarAst(salida);
    }

    private void limpiarPaneles() {
        panelPrompt.setText("");
        for (DefaultTableModel modelo : new DefaultTableModel[] {modeloTokens, modeloErrores,
            modeloSimbolos, modeloEstadisticas}) {
            modelo.setRowCount(0);
        }
        rotular(scrollTokens, "Tokens");
        rotular(scrollErrores, "Errores");
        rotular(scrollSimbolos, "Símbolos");
        mostrarEsquema(null);
        mostrarAst(null);
    }

    private void mostrarEsquema(ResultadoCompilacion resultado) {
        raizEsquema.removeAllChildren();
        if (resultado != null) {
            DefaultMutableTreeNode nodoDirectivas = new DefaultMutableTreeNode("Directivas");
            DefaultMutableTreeNode nodoSimbolos = new DefaultMutableTreeNode("Símbolos");
            DefaultMutableTreeNode nodoComandos = new DefaultMutableTreeNode("Comandos y Acciones");

            for (Token t : resultado.getTokens()) {
                String tipo = t.getTipo().toString();
                if (tipo.equals("DIRECTIVA")) {
                    nodoDirectivas.add(new DefaultMutableTreeNode(t.getLexema() + " (L" + t.getFila() + ")"));
                } else if (tipo.equals("COMANDO_IA") || tipo.equals("EJECUTAR") || tipo.equals("EXPORTAR")) {
                    nodoComandos.add(new DefaultMutableTreeNode(t.getLexema() + " (L" + t.getFila() + ")"));
                }
            }
            for (Simbolo s : resultado.getTabla().getSimbolos()) {
                nodoSimbolos.add(new DefaultMutableTreeNode(
                        s.getNombre() + " [" + s.getTipo().getEtiqueta() + "] (L" + s.getFila() + ")"));
            }
            raizEsquema.add(nodoDirectivas);
            raizEsquema.add(nodoSimbolos);
            raizEsquema.add(nodoComandos);
        }
        modeloEsquema.reload();
        for (int i = 0; i < arbolEsquema.getRowCount(); i++) {
            arbolEsquema.expandRow(i);
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

    private void mostrarEstadisticas(ResultadoCompilacion resultado, int totalLineas) {
        modeloEstadisticas.setRowCount(0);
        modeloEstadisticas.addRow(new Object[] {"Total de tokens", resultado.getTokens().size()});
        modeloEstadisticas.addRow(new Object[] {"Total de líneas", totalLineas});
        modeloEstadisticas.addRow(new Object[] {"Total de errores", resultado.getErrores().size()});
        Map<String, Integer> frecuencia = new LinkedHashMap<>();
        for (Token t : resultado.getTokens()) {
            frecuencia.merge(t.getTipo().toString(), 1, Integer::sum);
        }
        frecuencia.forEach((tipo, cantidad) -> modeloEstadisticas.addRow(new Object[] {tipo, cantidad}));
    }

    private void mostrarAst(Salida salida) {
        if (salida != null && salida.imagenAst != null) {
            etiquetaAst.setText(null);
            if (Math.abs(salida.zoomAst - 1.0) < 0.01) {
                etiquetaAst.setIcon(new ImageIcon(salida.imagenAst));
            } else {
                int ancho = Math.max(1, (int) (salida.imagenAst.getWidth() * salida.zoomAst));
                int alto = Math.max(1, (int) (salida.imagenAst.getHeight() * salida.zoomAst));
                Image escalada = salida.imagenAst.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                etiquetaAst.setIcon(new ImageIcon(escalada));
            }
        } else {
            etiquetaAst.setIcon(null);
            etiquetaAst.setText(salida == null ? TEXTO_AST_VACIO : salida.mensajeAst);
        }
    }

    private void generarImagenAst(Salida salida) {
        mostrarAst(salida);
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                Path dot = Files.createTempFile("promptzal_ast_", ".dot");
                Path png = Files.createTempFile("promptzal_ast_", ".png");
                try {
                    new MostrarGraphviz().compilarImagen(
                            new GeneradorDotAst().generarCodigo(salida.resultado.getPrograma()), dot, png);
                    return ImageIO.read(png.toFile());
                } finally {
                    Files.deleteIfExists(dot);
                    Files.deleteIfExists(png);
                }
            }

            @Override
            protected void done() {
                try {
                    salida.imagenAst = get();
                    if (salida.imagenAst == null) {
                        salida.mensajeAst = "No se pudo leer la imagen generada.";
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException ex) {
                    salida.mensajeAst = "No se pudo generar la imagen del AST (¿Graphviz instalado?): "
                            + mensajeDe(ex.getCause());
                }
                if (salidas.get(getPestanaActual()) == salida) mostrarAst(salida);
                
                if (salidas.get(getPestanaActual()) == salida) {
                    mostrarAst(salida);
                    actualizarBotonAst(salida);
                }
            }
        }.execute();
    }

    private void rotular(JScrollPane panel, String texto) {
        pestanasInferior.rotular(panel, texto);
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
                PestanaEditor pestana = getPestanaActual();
                if (e.getClickCount() != 2 || fila < 0 || pestana == null) return;
                int linea = (Integer) tabla.getValueAt(fila, columnaFila);
                int columna = columnaColumna < 0 ? 1 : (Integer) tabla.getValueAt(fila, columnaColumna);
                pestana.irA(linea, columna);
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
        PestanaEditor actual = getPestanaActual();
        Salida salida = salidas.get(actual);
        if (salida == null) {
            JOptionPane.showMessageDialog(this, "Primero debe compilar este archivo antes de generar reportes.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ResultadoCompilacion resultado = salida.resultado;
        JFileChooser chooser = selector();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, "Elegir carpeta") != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);
        Path carpeta = chooser.getSelectedFile().toPath();

        String sufijo = sufijoUnico(actual);
        Path htmlErrores = asegurarNoSobrescribir(carpeta.resolve("reporte_errores_" + sufijo + ".html"));
        Path htmlSimbolos = asegurarNoSobrescribir(carpeta.resolve("reporte_simbolos_" + sufijo + ".html"));
        Path htmlAst = asegurarNoSobrescribir(carpeta.resolve("reporte_ast_" + sufijo + ".html"));

        boolean ok = escribir(htmlErrores, new ReporteErrores().generar(resultado.getErrores()));
        ok &= escribir(htmlSimbolos, new ReporteSimbolos().generar(resultado.getTabla()));
        ok &= escribir(htmlAst, new ReporteAst().generar(resultado.getPrograma()));
        if (!ok) return;

        boolean imagen = guardarImagenAst(carpeta, resultado, sufijo);
        etiquetaEstado.setText(" Reportes generados en: " + carpeta);

        if (Desktop.isDesktopSupported() && JOptionPane.showConfirmDialog(this,
                "Reportes generados en:\n" + carpeta
                + "\n- " + htmlErrores.getFileName()
                + "\n- " + htmlSimbolos.getFileName()
                + "\n- " + htmlAst.getFileName()
                + (imagen ? "" : "\n(No se generó la imagen del AST porque Graphviz no está disponible.)")
                + "\n\n¿Desea abrir los reportes HTML en el navegador?",
                "Reportes generados", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().browse(htmlErrores.toUri());
                Desktop.getDesktop().browse(htmlSimbolos.toUri());
                Desktop.getDesktop().browse(htmlAst.toUri());
            } catch (IOException ex) {
                mensaje("No se pudieron abrir los reportes automáticamente: " + ex.getMessage());
            }
        }
    }

    private boolean guardarImagenAst(Path carpeta, ResultadoCompilacion resultado, String sufijo) {
        try {
            Path dot = asegurarNoSobrescribir(carpeta.resolve("reporte_ast_" + sufijo + ".dot"));
            Path png = asegurarNoSobrescribir(carpeta.resolve("reporte_ast_" + sufijo + ".png"));
            new MostrarGraphviz().compilarImagen(new GeneradorDotAst().generarCodigo(resultado.getPrograma()), dot, png);
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
    
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private String sufijoUnico(PestanaEditor pestana) {
        String base = (pestana == null || pestana.getNombre() == null || pestana.getNombre().isBlank())
                ? "programa"
                : pestana.getNombre().trim();
        if (base.toLowerCase().endsWith(".pz")) {
            base = base.substring(0, base.length() - 3);
        }
        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        return base + "_" + LocalDateTime.now().format(FORMATO_FECHA_HORA);
    }

    private Path asegurarNoSobrescribir(Path ruta) {
        if (!Files.exists(ruta)) return ruta;
        String nombre = ruta.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        String ext = punto > 0 ? nombre.substring(punto) : "";
        int contador = 1;
        Path candidata = ruta;
        while (Files.exists(candidata)) {
            candidata = ruta.resolveSibling(base + "_" + contador + ext);
            contador++;
        }
        return candidata;
    }
    
    private void actualizarBotonAst(Salida salida) {
        boolean listo = salida != null && salida.imagenAst != null;

        Color colorVer = listo ? Tema.LIMA : Color.GRAY;
        String ayudaVer = listo
                ? "Genera y abre la imagen del AST de este archivo"
                : "Compila este archivo primero para habilitar el AST";
        botonAstActual = boton("Generar AST", ayudaVer, colorVer, this::verAST);
        botonAstActual.setEnabled(listo);
        if (!listo) {
            botonAstActual.setForeground(Color.GRAY);
        }
        contenedorBotonAst.removeAll();
        contenedorBotonAst.add(botonAstActual, BorderLayout.CENTER);
        contenedorBotonAst.revalidate();
        contenedorBotonAst.repaint();

        Color colorGuardar = listo ? Tema.CIAN : Color.GRAY;
        String ayudaGuardar = listo
                ? "Guarda la imagen PNG del AST de este archivo"
                : "Compila este archivo primero para poder guardar su AST";
        botonGuardarAstActual = boton("Guardar AST", ayudaGuardar, colorGuardar, this::guardarAstImagen);
        botonGuardarAstActual.setEnabled(listo);
        if (!listo) {
            botonGuardarAstActual.setForeground(Color.GRAY);
        }
        contenedorBotonGuardarAst.removeAll();
        contenedorBotonGuardarAst.add(botonGuardarAstActual, BorderLayout.CENTER);
        contenedorBotonGuardarAst.revalidate();
        contenedorBotonGuardarAst.repaint();
    }
    
    //guardar el ast
    private void guardarAstImagen() {
        PestanaEditor actual = getPestanaActual();
        Salida salida = salidas.get(actual);
        if (salida == null || salida.imagenAst == null) {
            mensaje("Primero debes compilar este archivo para guardar su AST.");
            return;
        }
        String sufijo = sufijoUnico(actual);
        FileNameExtensionFilter filtroPng = new FileNameExtensionFilter("Imagen PNG (*.png)", "png");
        JFileChooser chooser = selector();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filtroPng);
        chooser.setFileFilter(filtroPng);
        chooser.setSelectedFile(new File("ast_" + sufijo + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        recordarDirectorio(chooser);

        Path rutaPng = asegurarNoSobrescribir(conExtension(chooser.getSelectedFile().toPath(), ".png"));
        try {
            ImageIO.write(salida.imagenAst, "png", rutaPng.toFile());
            etiquetaEstado.setText(" Imagen AST guardada: " + rutaPng.getFileName());
            JOptionPane.showMessageDialog(this,
                    "Imagen del AST guardada exitosamente en:\n" + rutaPng.toAbsolutePath(),
                    "Guardar AST", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mensaje("No se pudo guardar la imagen del AST: " + ex.getMessage());
        }
    }

    private void verAST() {
        PestanaEditor actual = getPestanaActual();
        Salida salida = salidas.get(actual);
        if (salida == null || salida.imagenAst == null) {
            mensaje("Primero debes compilar este archivo para generar su AST.");
            return;
        }
        try {
            if (salida.rutaImagenAst == null || !Files.exists(salida.rutaImagenAst)) {
                Path carpeta = Path.of("reportes");
                Files.createDirectories(carpeta);
                String sufijo = sufijoUnico(actual);
                Path dot = asegurarNoSobrescribir(carpeta.resolve("ast_" + sufijo + ".dot"));
                Path png = asegurarNoSobrescribir(carpeta.resolve("ast_" + sufijo + ".png"));
                new MostrarGraphviz().compilarImagen(
                        new GeneradorDotAst().generarCodigo(salida.resultado.getPrograma()), dot, png);
                salida.rutaImagenAst = png;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(salida.rutaImagenAst.toFile());
            }
            etiquetaEstado.setText(" Imagen del AST abierta: " + salida.rutaImagenAst.toAbsolutePath());
        } catch (IOException ex) {
            mensaje("No se pudo generar o abrir el AST:\n" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}