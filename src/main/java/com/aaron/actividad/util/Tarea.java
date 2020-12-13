package com.aaron.actividad.util;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class Tarea extends Thread{
    private Label lbMensaje;
    private String mensaje;

    public Tarea(Label lbMensaje, String mensaje) {
        this.lbMensaje = lbMensaje;
        this.mensaje = mensaje;
    }

    @Override
    public void run(){
        for(int i=0;i<=3;i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lbMensaje.setStyle("-fx-font-weight: bold");
            lbMensaje.setStyle("-fx-text-fill: red");
            Platform.runLater(()-> lbMensaje.setText(mensaje));
        }
        mensaje="";
        Platform.runLater(()->lbMensaje.setText(mensaje));
    }
}