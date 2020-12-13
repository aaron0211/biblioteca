package com.aaron.actividad;

import com.aaron.actividad.domain.Usuarios;
import com.aaron.actividad.util.R;
import com.aaron.actividad.util.Tarea;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController{

    public TextField tfUserRegis,tfUserLogin;
    public PasswordField psPassRegis, psPassLogin;
    public Label lbMensaje, lbMensajeLogin;

    private UsuariosDAO usuariosDAO;
    private String mensaje;
    private Stage pantalla;

    public LoginController(Stage pantalla){
        usuariosDAO = new UsuariosDAO();
        this.pantalla = pantalla;
        try {
            usuariosDAO.conectar();
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
    }

    @FXML
    public void registrar(Event event) throws SQLException {
        String usuario = tfUserRegis.getText();
        String pass = psPassRegis.getText();
        if (usuario.equals("") || pass.equals("")){
            mensaje = "Nombre y contraseña OBLIGATORIOS";
            Tarea tarea = new Tarea(lbMensaje,mensaje);
            tarea.start();
            return;
        }
        Usuarios usuarios = new Usuarios(usuario,pass);
        String lstUsuarios = usuariosDAO.comprobarUsuario(usuario);
        if (lstUsuarios!=null){
            mensaje = "El Usuario ya existe";
            Tarea tarea = new Tarea(lbMensaje,mensaje);
            tarea.start();
            return;
        }
        usuariosDAO.nuevoUsuario(usuarios);
        mensaje = usuario +" registrado con éxito";
        Tarea tarea = new Tarea(lbMensaje,mensaje);
        tarea.start();
        tfUserRegis.setText("");
        psPassRegis.setText("");
    }

    @FXML
    public void entrar(Event event) throws Exception {
        String usuario = tfUserLogin.getText();
        String pass = psPassLogin.getText();

        Usuarios usuarios = new Usuarios(usuario,pass);
        Usuarios lista = usuariosDAO.loginUsuario(usuarios);
        if (usuario.equals(lista.getUsuario()) && pass.equals(lista.getPass())){
            FXMLLoader loader = new FXMLLoader();
            BibliotecaController controller = new BibliotecaController();
            loader.setLocation(R.getUI("biblioteca.fxml"));
            loader.setController(controller);
            VBox vBox = loader.load();

            Scene scene = new Scene(vBox);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            pantalla.close();
            usuariosDAO.desconectar();
        }else {
            mensaje = "Usuario o Contraseña ERRÓNEA";
            Tarea tarea = new Tarea(lbMensajeLogin,mensaje);
            tarea.start();
        }
    }
}
