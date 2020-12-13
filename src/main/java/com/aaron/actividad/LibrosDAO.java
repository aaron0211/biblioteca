package com.aaron.actividad;

import com.aaron.actividad.domain.Libros;
import com.aaron.actividad.util.R;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

public class LibrosDAO {

    private Connection conexion;
    private enum Accion{
        MYSQL, POSTGRESQL
    }
    private Accion accion;

    public void conec() throws SQLException, IOException, ClassNotFoundException {
        if (accion != null) {
            switch (accion) {
                case MYSQL:
                    this.desconectar();
                    conectar();
                    accion = Accion.POSTGRESQL;
                    break;
                case POSTGRESQL:
                    this.desconectar();
                    conectarPostgre();
                    accion = Accion.MYSQL;
            }
        }else {
            conectar();
            accion = Accion.POSTGRESQL;
        }
    }

    public void conectar() throws ClassNotFoundException, SQLException, IOException {
        Properties configuration = new Properties();
        configuration.load(R.getProperties("database.properties"));
        String host = configuration.getProperty("host");
        String port = configuration.getProperty("port");
        String name = configuration.getProperty("name");
        String username = configuration.getProperty("username");
        String password = configuration.getProperty("password");

        Class.forName("com.mysql.cj.jdbc.Driver");
        conexion = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?serverTimezone=UTC",
                username, password);
    }

    public void desconectar() throws SQLException{
        conexion.close();
    }

    public void conectarPostgre() throws ClassNotFoundException, SQLException, IOException{
        Properties configuracion = new Properties();
        configuracion.load(R.getProperties("database.properties"));
        String host = configuracion.getProperty("postgreHost");
        String port = configuracion.getProperty("postgrePort");
        String name = configuracion.getProperty("postgreName");
        String username = configuracion.getProperty("postgreUsername");
        String password = configuracion.getProperty("postgrePassword");

        Class.forName("org.postgresql.Driver");
        conexion = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + name,username,password);
    }

    public void nuevoLibro(Libros libro) throws SQLException{
        String sql = "INSERT INTO libros (titulo,autor,isbn,genero) VALUES (?,?,?,?)";

        PreparedStatement sentencia = conexion.prepareStatement(sql);
        sentencia.setString(1,libro.getTitulo());
        sentencia.setString(2,libro.getAutor());
        sentencia.setString(3,libro.getIsbn());
        sentencia.setString(4,libro.getGenero());
        sentencia.executeUpdate();
        System.out.println(sentencia.toString());
    }

    public List<Libros> mostrarRegistro() throws SQLException {
        String sql = "SELECT * from libros";
        List<Libros> lista = new ArrayList<>();

        PreparedStatement sentencia = conexion.prepareStatement(sql);
        ResultSet resultSet = sentencia.executeQuery();

        while (resultSet.next()){
            Libros libros = new Libros();
            switch (accion){
                case MYSQL:
                    libros.setId(resultSet.getInt(3));
                    libros.setTitulo(resultSet.getString(1));
                    libros.setAutor(resultSet.getString(4));
                    libros.setIsbn(resultSet.getString(2));
                    libros.setGenero(resultSet.getString(5));
                    break;
                case POSTGRESQL:
                    libros.setId(resultSet.getInt(1));
                    libros.setTitulo(resultSet.getString(2));
                    libros.setAutor(resultSet.getString(3));
                    libros.setIsbn(resultSet.getString(4));
                    libros.setGenero(resultSet.getString(5));
            }
            lista.add(libros);
        }
        System.out.println(lista.toString());
        return lista;
    }

    public void modificarLibro(Libros anterior, Libros nuevo) throws SQLException{
        String sql = "UPDATE libros set titulo = ?, autor = ?, isbn = ?," +
                " genero = ? where id = ?";
        PreparedStatement sentencia = conexion.prepareStatement(sql);
        sentencia.setString(1,nuevo.getTitulo());
        sentencia.setString(2,nuevo.getAutor());
        sentencia.setString(3,nuevo.getIsbn());
        sentencia.setString(4,nuevo.getGenero());
        sentencia.setInt(5,anterior.getId());
        sentencia.executeUpdate();
    }

    public void eliminarLibro(Libros libros) throws SQLException{
        String sql = "DELETE from libros where id = ?";
        PreparedStatement sentencia = conexion.prepareStatement(sql);
        sentencia.setInt(1,libros.getId());
        sentencia.executeUpdate();
    }

    public List<Libros> buscarRegistro(String titulo) throws SQLException{
        List<Libros> lista = new ArrayList<>();
        switch (accion){
            case POSTGRESQL:
                String sql = "SELECT * FROM libros where titulo regexp ?";

                PreparedStatement sentencia = conexion.prepareStatement(sql);
                sentencia.setString(1,titulo);
                ResultSet resultSet = sentencia.executeQuery();

                while (resultSet.next()){
                    Libros listLibros = new Libros();
                    listLibros.setTitulo(resultSet.getString(2));
                    listLibros.setAutor(resultSet.getString(3));
                    listLibros.setIsbn(resultSet.getString(4));
                    listLibros.setGenero(resultSet.getString(5));
                    lista.add(listLibros);
                }
                break;
            case MYSQL:
                String sqlp = "SELECT titulo, autor, isbn, genero, REGEXP_MATCHES(titulo, ?) from libros";

                PreparedStatement sentencia2 = conexion.prepareStatement(sqlp);
                sentencia2.setString(1,titulo);
                ResultSet resultSet1 = sentencia2.executeQuery();

                while (resultSet1.next()){
                    Libros listLibros = new Libros();
                    listLibros.setTitulo(resultSet1.getString(1));
                    listLibros.setAutor(resultSet1.getString(2));
                    listLibros.setIsbn(resultSet1.getString(3));
                    listLibros.setGenero(resultSet1.getString(4));
                    lista.add(listLibros);
                }
                break;
        }
        return lista;
    }

    public void borrarTodo() throws SQLException{
        String sql = "DELETE FROM libros";
        PreparedStatement sentencia = conexion.prepareStatement(sql);
        sentencia.executeUpdate();
    }
}
