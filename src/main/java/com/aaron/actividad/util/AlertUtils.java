package com.aaron.actividad.util;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static void mostrarAlerta(String mensaje){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }
}
