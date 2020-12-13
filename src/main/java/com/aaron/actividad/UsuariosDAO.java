package com.aaron.actividad;

import com.aaron.actividad.domain.Usuarios;

import java.sql.*;

public class UsuariosDAO {

    private Connection conexion;

    public void conectar() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/biblioteca?serverTimezone=UTC",
                "root", "");
    }

    public void desconectar() throws SQLException{
        conexion.close();
    }

    public void nuevoUsuario(Usuarios usuarios) throws SQLException{
        String sql = "INSERT INTO usuarios (usuario,pass) VALUES (?,?)";

        PreparedStatement sentencia = conexion.prepareStatement(sql);
        sentencia.setString(1,usuarios.getUsuario());
        sentencia.setString(2,usuarios.getPass());
        sentencia.executeUpdate();
    }

    public String comprobarUsuario(String usuario){
        String sql = "SELECT * FROM usuarios where usuario = ?";
        String nombre = null;
        try {
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            sentencia.setString(1,usuario);
            ResultSet resultSet = sentencia.executeQuery();

            while (resultSet.next()){
                nombre = (resultSet.getString(2));
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return nombre;
    }

    public Usuarios loginUsuario(Usuarios usuarios){
        String sql = "SELECT * FROM usuarios where usuario = ? and pass = ?";
        Usuarios lUsuarios = new Usuarios();
        try {
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            sentencia.setString(1,usuarios.getUsuario());
            sentencia.setString(2,usuarios.getPass());
            ResultSet resultSet = sentencia.executeQuery();

            while (resultSet.next()){
                lUsuarios.setUsuario(resultSet.getString(2));
                lUsuarios.setPass(resultSet.getString(3));
            }
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return lUsuarios;
    }
}
