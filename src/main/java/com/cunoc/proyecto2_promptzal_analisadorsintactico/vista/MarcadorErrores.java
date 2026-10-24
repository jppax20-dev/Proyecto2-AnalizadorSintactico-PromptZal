package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import com.cunoc.proyecto2_promptzal_analisadorsintactico.errores.ErrorCompilacion;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public final class MarcadorErrores {

    private static final String PROP_ERRORES = "promptzal.errores.mapa";
    private static final String PROP_TAGS = "promptzal.errores.tags";
    private static final String PROP_INSTALADO = "promptzal.errores.instalado";
    private static final Color COLOR_ERROR = new Color(255, 65, 85);
    private static final Highlighter.HighlightPainter SUBRAYADO_ONDULADO = new SubrayadoOnduladoPainter(COLOR_ERROR);

    private MarcadorErrores() {
    }

    public static void marcar(PestanaEditor pestana, List<ErrorCompilacion> errores) {
        if (pestana == null || !(pestana.getEditor() instanceof JTextComponent editor)) return;
        instalarSiFalta(pestana, editor);
        limpiar(pestana);

        if (errores == null || errores.isEmpty()) {
            pestana.repaint();
            return;
        }

        Map<Integer, StringBuilder> mapaPorLinea = new LinkedHashMap<>();
        List<Object> tags = new ArrayList<>();
        Element raiz = editor.getDocument().getDefaultRootElement();

        for (ErrorCompilacion err : errores) {
            int fila = Math.max(1, err.getFila());
            int col = Math.max(1, err.getColumna());

            mapaPorLinea.computeIfAbsent(fila, k -> new StringBuilder("<html>"))
                    .append("<b>[")
                    .append(err.getTipo())
                    .append("]</b> ")
                    .append(escaparHtml(err.getDescripcion()))
                    .append(" <i>(L")
                    .append(fila)
                    .append(":C")
                    .append(col)
                    .append(")</i><br>");

            int indiceLinea = fila - 1;
            if (indiceLinea >= 0 && indiceLinea < raiz.getElementCount()) {
                Element elem = raiz.getElement(indiceLinea);
                int inicioLinea = elem.getStartOffset();
                int finLinea = elem.getEndOffset() - 1;
                int inicio = Math.min(inicioLinea + (col - 1), finLinea);
                int fin = calcularFinToken(editor, inicio, finLinea, inicioLinea);
                try {
                    tags.add(editor.getHighlighter().addHighlight(inicio, fin, SUBRAYADO_ONDULADO));
                } catch (BadLocationException ignored) {
                }
            }
        }

        Map<Integer, String> mensajesFinales = new LinkedHashMap<>();
        mapaPorLinea.forEach((linea, sb) -> {
            if (sb.length() >= 4 && sb.substring(sb.length() - 4).equals("<br>")) {
                sb.setLength(sb.length() - 4);
            }
            sb.append("</html>");
            mensajesFinales.put(linea, sb.toString());
        });

        editor.putClientProperty(PROP_ERRORES, mensajesFinales);
        editor.putClientProperty(PROP_TAGS, tags);
        pestana.repaint();
    }

    @SuppressWarnings("unchecked")
    public static void limpiar(PestanaEditor pestana) {
        if (pestana == null || !(pestana.getEditor() instanceof JTextComponent editor)) return;
        List<Object> tags = (List<Object>) editor.getClientProperty(PROP_TAGS);
        if (tags != null) {
            for (Object tag : tags) {
                editor.getHighlighter().removeHighlight(tag);
            }
            tags.clear();
        }
        editor.putClientProperty(PROP_ERRORES, null);
        editor.setToolTipText(null);
        pestana.repaint();
    }

    private static int calcularFinToken(JTextComponent editor, int inicio, int finLinea, int inicioLinea) {
        try {
            String texto = editor.getDocument().getText(inicio, Math.max(0, finLinea - inicio));
            int longitud = 0;
            while (longitud < texto.length() && !Character.isWhitespace(texto.charAt(longitud))) {
                longitud++;
            }
            if (longitud > 0) {
                return inicio + longitud;
            }
            String lineaCompleta = editor.getDocument().getText(inicioLinea, Math.max(0, finLinea - inicioLinea));
            int primerNoEspacio = 0;
            while (primerNoEspacio < lineaCompleta.length() && Character.isWhitespace(lineaCompleta.charAt(primerNoEspacio))) {
                primerNoEspacio++;
            }
            if (primerNoEspacio < lineaCompleta.length()) {
                return inicioLinea + lineaCompleta.length();
            }
        } catch (BadLocationException ignored) {
        }
        return Math.min(inicio + 1, editor.getDocument().getLength());
    }

    private static void instalarSiFalta(PestanaEditor pestana, JTextComponent editor) {
        if (Boolean.TRUE.equals(editor.getClientProperty(PROP_INSTALADO))) return;
        editor.putClientProperty(PROP_INSTALADO, Boolean.TRUE);

        editor.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void mouseMoved(MouseEvent e) {
                Map<Integer, String> mapa = (Map<Integer, String>) editor.getClientProperty(PROP_ERRORES);
                if (mapa == null || mapa.isEmpty()) {
                    editor.setToolTipText(null);
                    return;
                }
                int pos = editor.viewToModel2D(e.getPoint());
                if (pos >= 0) {
                    int linea = editor.getDocument().getDefaultRootElement().getElementIndex(pos) + 1;
                    editor.setToolTipText(mapa.get(linea));
                }
            }
        });

        JScrollPane scroll = buscarScrollPane(pestana, editor);
        if (scroll != null && scroll.getRowHeader() != null) {
            Component vista = scroll.getRowHeader().getView();
            if (vista instanceof JComponent comp && !(vista instanceof JLayer<?>)) {
                JLayer<JComponent> capa = new JLayer<>(comp, new CapaGutterErrores(editor));
                scroll.setRowHeaderView(capa);
            }
        }
    }

    private static JScrollPane buscarScrollPane(Component raiz, JTextComponent editor) {
        JScrollPane directo = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editor);
        if (directo != null) return directo;
        if (raiz instanceof JScrollPane sp) return sp;
        return null;
    }

    private static String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class SubrayadoOnduladoPainter implements Highlighter.HighlightPainter {
        private final Color color;

        private SubrayadoOnduladoPainter(Color color) {
            this.color = color;
        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            try {
                Rectangle r0 = c.modelToView2D(offs0).getBounds();
                Rectangle r1 = c.modelToView2D(offs1).getBounds();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor( new Color(color.getRed(), color.getGreen(), color.getBlue(), 35) );

                int yBase = r0.y + r0.height - 2;
                int xInicio = r0.x;
                int xFin = (r0.y == r1.y) ? r1.x : c.getWidth() - c.getInsets().right;
                if (xFin <= xInicio) {
                    xFin = xInicio + 14;
                }

                g2.fillRect(xInicio, r0.y, xFin - xInicio, r0.height);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.4f));
                int paso = 3;
                int amplitud = 2;
                boolean arriba = true;
                for (int x = xInicio; x < xFin; x += paso) {
                    int y1 = arriba ? yBase - amplitud : yBase + amplitud;
                    int y2 = arriba ? yBase + amplitud : yBase - amplitud;
                    g2.drawLine(x, y1, Math.min(x + paso, xFin), y2);
                    arriba = !arriba;
                }
                g2.dispose();
            } catch (BadLocationException ignored) {
            }
        }
    }

    private static final class CapaGutterErrores extends LayerUI<JComponent> {
        private final JTextComponent editor;

        private CapaGutterErrores(JTextComponent editor) {
            this.editor = editor;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            if (c instanceof JLayer<?> layer) {
                layer.setLayerEventMask(java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);
            }
        }

        @Override
        public void uninstallUI(JComponent c) {
            if (c instanceof JLayer<?> layer) {
                layer.setLayerEventMask(0);
            }
            super.uninstallUI(c);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
            Map<Integer, String> mapa = (Map<Integer, String>) editor.getClientProperty(PROP_ERRORES);
            if (mapa == null || mapa.isEmpty()) {
                l.getView().setToolTipText(null);
                return;
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l.getView());
            int pos = editor.viewToModel2D(new Point(0, p.y));
            if (pos >= 0) {
                int linea = editor.getDocument().getDefaultRootElement().getElementIndex(pos) + 1;
                l.getView().setToolTipText(mapa.get(linea));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            Map<Integer, String> mapa = (Map<Integer, String>) editor.getClientProperty(PROP_ERRORES);
            if (mapa == null || mapa.isEmpty()) return;

            Element raiz = editor.getDocument().getDefaultRootElement();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Integer linea : mapa.keySet()) {
                int idx = linea - 1;
                if (idx < 0 || idx >= raiz.getElementCount()) continue;
                try {
                    Rectangle rect = editor.modelToView2D(raiz.getElement(idx).getStartOffset()).getBounds();
                    int tam = 13;
                    int x = 3;
                    int y = rect.y + (rect.height - tam) / 2;

                    g2.setColor(COLOR_ERROR);
                    g2.fillOval(x, y, tam, tam);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (tam - fm.stringWidth("!")) / 2;
                    int ty = y + ((tam - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString("!", tx, ty);
                } catch (BadLocationException ignored) {
                }
            }
            g2.dispose();
        }
    }
}