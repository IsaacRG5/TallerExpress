package com.tallerexpress.view;

import javax.swing.JOptionPane;

/**
 * Utilidad sencilla para mensajes de la interfaz JOptionPane.
 * La lógica de negocio permanece en service y repository.
 */
public final class AppView {
    private AppView() { }

    public static void info(String message) {
        JOptionPane.showMessageDialog(null, message, "VetCare", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(String message) {
        JOptionPane.showMessageDialog(null, message, "VetCare - Error", JOptionPane.ERROR_MESSAGE);
    }
}
