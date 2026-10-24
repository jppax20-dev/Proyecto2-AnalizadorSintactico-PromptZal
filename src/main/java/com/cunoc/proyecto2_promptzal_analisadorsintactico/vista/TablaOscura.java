package com.cunoc.proyecto2_promptzal_analisadorsintactico.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

class TablaOscura extends JTable {

    TablaOscura(TableModel modelo) {
        super(modelo);
        setBackground(Tema.FONDO);
        setForeground(Tema.TEXTO);
        setSelectionBackground(Tema.SELECCION);
        setSelectionForeground(Color.WHITE);
        setShowGrid(false);
        getTableHeader().setDefaultRenderer(new EncabezadoOscuro());
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderizador, int fila, int columna) {
        Component celda = super.prepareRenderer(renderizador, fila, columna);
        if (!isRowSelected(fila)) {
            celda.setBackground(fila % 2 == 0 ? Tema.FONDO : Tema.PANEL);
        }
        return celda;
    }

    private static final class EncabezadoOscuro extends DefaultTableCellRenderer {

        private static final Border BORDE_ENCABEZADO = BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.CIAN),
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDE)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8));

        EncabezadoOscuro() {
            setOpaque(true);
            setBackground(Tema.BASE);
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, false, false, fila, columna);
            setBorder(BORDE_ENCABEZADO);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
}