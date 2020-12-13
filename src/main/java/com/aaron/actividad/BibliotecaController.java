package com.aaron.actividad;

import com.aaron.actividad.domain.Libros;
import com.aaron.actividad.util.AlertUtils;
import com.aaron.actividad.util.Tarea;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BibliotecaController implements Initializable {
    public TextField tfTitulo, tfAutor, tfIsbn, tfBuscar;
    public ComboBox<String> cbGenero;
    public Label lbInfo;
    public TableView<Libros> tvRegistro;
    public Button btNuevo, btGuardar, btModificar, btEliminar, btCancelar;

    private LibrosDAO librosDAO;
    private Libros libroSeleccionado;
    private String mensaje;
    private Libros eliminado;

    private static final Logger logger = LogManager.getLogger(BibliotecaController.class);

    private enum Accion {
        NUEVO, MODIFICAR
    }
    private Accion accion;

    public BibliotecaController(){
        logger.debug("Aplicación Iniciada");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        librosDAO = new LibrosDAO();
        try {
            librosDAO.conec();
        }catch (SQLException sqle){
            logger.error("Fallo al conectar",sqle.fillInStackTrace());
            AlertUtils.mostrarAlerta("Fallo al conectar");
        }catch (ClassNotFoundException cnfe){
            logger.error("Fallo al conectar",cnfe.fillInStackTrace());
            AlertUtils.mostrarAlerta("Fallo al conectar");
        }catch (IOException ioe){
            logger.error("Fallo al conectar",ioe.fillInStackTrace());
            AlertUtils.mostrarAlerta("Fallo al conectar");
        }
        fijarColumnas();
        cargarLista();
    }

    public void cargarLista(){
        editarBoton(false);
        tvRegistro.getItems().clear();
        try {
            List<Libros> lista = librosDAO.mostrarRegistro();
            tvRegistro.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            //AlertUtils.mostrarAlerta("Fallo al cargar la lista");
        }
        String[] genero = new String[]{"<Selecciona>","Novela","Ficción","Poesia","Histórico","Aventura"};
        cbGenero.setItems(FXCollections.observableArrayList(genero));
    }

    private void fijarColumnas(){
        Field[] fields = Libros.class.getDeclaredFields();
        for (Field field: fields){
            if (field.getName().equals("id"))
                continue;

            TableColumn<Libros,String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvRegistro.getColumns().add(column);
        }
        tvRegistro.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    public void nuevoLibro(Event event){
        limpiar();
        editarBoton(true);
        accion = Accion.NUEVO;
    }

    @FXML
    public void modificarLibro(Event event){
        editarBoton(true);
        accion = Accion.MODIFICAR;
    }

    @FXML
    public void eliminarLibro(Event event){
        Libros libro = tvRegistro.getSelectionModel().getSelectedItem();
        eliminado = libro;
        if (libro==null){
            mensaje = "Selecciona un libro";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
        }
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar libro");
            confirmacion.setContentText("¿Estás seguro?");
            Optional<ButtonType> resp = confirmacion.showAndWait();
            if (resp.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                return;
            librosDAO.eliminarLibro(libro);
            mensaje ="Libro eliminado";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
            cargarLista();
        }catch (SQLException sqle){
            AlertUtils.mostrarAlerta("No se ha podido eliminar el libro");
        }
    }

    @FXML
    public void guardarLibro(Event event){
        String titulo = tfTitulo.getText();
        if (titulo.equals("")){
            mensaje = "Titulo obligatorio";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
            return;
        }
        String autor = tfAutor.getText();
        if (autor.equals("")){
            mensaje = "Autor obligatorio";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
            return;
        }
        String isbn = tfIsbn.getText();
        String genero = cbGenero.getSelectionModel().getSelectedItem();
        Libros libro = new Libros(titulo,autor,isbn,genero);

        try {
            switch (accion){
                case NUEVO:
                    librosDAO.nuevoLibro(libro);
                    mensaje = "Libro añadido";
                    break;
                case MODIFICAR:
                    librosDAO.modificarLibro(libroSeleccionado,libro);
                    mensaje = "Libro actualizado";
                    break;
            }
        } catch (SQLException sqlException) {
            AlertUtils.mostrarAlerta("Fallo al añadir el libro");
        }
        cargarLista();
        Tarea tarea = new Tarea(lbInfo,mensaje);
        tarea.start();
        editarBoton(false);
    }

    @FXML
    public void seleccionarRegistro(Event event){
        libroSeleccionado = tvRegistro.getSelectionModel().getSelectedItem();
        cargarLibro(libroSeleccionado);
    }

    @FXML
    public void buscar(Event event){
        String titulo = tfBuscar.getText();
        if (titulo.equals("")){
            mensaje = "Título obligatorio";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
            return;
        }
        try {
            List<Libros> lista = librosDAO.buscarRegistro(titulo);
            tvRegistro.getItems().clear();
            tvRegistro.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException sqlException) {
            AlertUtils.mostrarAlerta("Fallo al buscar");
        }
        tfBuscar.setText("");
    }

    @FXML
    public void cancelar(Event event){
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Edición");
        confirmacion.setContentText("¿Estás seguro?");
        Optional<ButtonType> resp = confirmacion.showAndWait();
        if (resp.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
            return;

        editarBoton(false);
        cargarLibro(libroSeleccionado);
    }

    @FXML
    public void borrarTodo(Event event){
        try {Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Eliminar Registros");
            confirmacion.setContentText("¿Estás seguro que quieres eliminar TODOS los registros?");
            Optional<ButtonType> resp = confirmacion.showAndWait();
            if (resp.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                return;
            librosDAO.borrarTodo();
            mensaje ="Registro eliminado";
            Tarea tarea = new Tarea(lbInfo,mensaje);
            tarea.start();
            cargarLista();
            logger.info("Borrados todos los registros");
        } catch (SQLException sqlException) {
            AlertUtils.mostrarAlerta("Fallo al borrar la base de datos");
        }
    }

    @FXML
    public void exportar(Event event){
        try {
            FileChooser fileChooser = new FileChooser();
            File fichero = fileChooser.showSaveDialog(btCancelar.getScene().getWindow());
            if (fichero == null) return;

            FileWriter fileWriter = new FileWriter(fichero);
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("id","titulo","autor","isbn","genero"));

            List<Libros> libros = tvRegistro.getItems();
            for (Libros libro : libros){
                printer.printRecord(libro.getId(),libro.getTitulo(),
                    libro.getAutor(),libro.getIsbn(),libro.getGenero());
            }
            printer.close();
        }catch (IOException ioe){
            logger.error("Fallo al exportar");
            AlertUtils.mostrarAlerta("Error al exportar datos");
        }
    }

    @FXML
    public void importar(Event event){
        try {
            FileChooser fileChooser = new FileChooser();
            File importar = fileChooser.showOpenDialog(null);

            BufferedReader br = new BufferedReader(new FileReader(importar));
            String linea = br.readLine();

            while (linea != null){
                String[] datos = linea.split(",");
                Libros libros = crearLibro(datos);
                librosDAO.nuevoLibro(libros);
                linea = br.readLine();
            }
        }catch (IOException | SQLException ioe){
            AlertUtils.mostrarAlerta("Error al importar datos");
        }
        cargarLista();
    }

    @FXML
    public void recuperar(Event event){
        if (eliminado!=null) {
            try {
                librosDAO.nuevoLibro(eliminado);
                mensaje = "Libro recuperado";
                cargarLista();
                Tarea tarea = new Tarea(lbInfo,mensaje);
                tarea.start();
                editarBoton(false);
                eliminado = null;
            } catch (SQLException sqlException) {
                AlertUtils.mostrarAlerta("No se ha podido recuperar");
            }
        }
    }

    @FXML
    public void volver(Event event){
        cargarLista();
    }

    @FXML
    public void cambiarBD(Event event){
        try {
            librosDAO.conec();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            logger.error("Fallo al cambiar de BBDD", sqlException.fillInStackTrace());
            //AlertUtils.mostrarAlerta("Fallo al conectar con la Base de datos");
        } catch (IOException e) {
            logger.error("Fallo al cambiar de BBDD",e.fillInStackTrace());
            e.printStackTrace();
            //AlertUtils.mostrarAlerta("Fallo al conectar con la Base de datos");
        } catch (ClassNotFoundException e) {
            logger.error("Fallo al cambiar de BBDD",e.fillInStackTrace());
            e.printStackTrace();
            //AlertUtils.mostrarAlerta("Fallo al conectar con la Base de datos");
        }
        cargarLista();
    }

    private static Libros crearLibro(String[] datos){
        String titulo = datos[1];
        String autor = datos[2];
        String isbn = datos[3];
        String genero = datos[4];

        return new Libros(titulo,autor,isbn,genero);
    }

    private void cargarLibro(Libros libro){
        tfTitulo.setText(libro.getTitulo());
        tfAutor.setText(libro.getAutor());
        tfIsbn.setText((libro.getIsbn()));
        cbGenero.setValue(libro.getGenero());
    }

    private void editarBoton(boolean activar){
        btNuevo.setDisable(activar);
        btGuardar.setDisable(!activar);
        btModificar.setDisable(activar);
        btEliminar.setDisable(activar);

        tfTitulo.setEditable(activar);
        tfAutor.setEditable(activar);
        tfIsbn.setEditable(activar);
        cbGenero.setDisable(!activar);

        tvRegistro.setEditable(activar);
    }

    private void limpiar(){
        tfTitulo.setText("");
        tfAutor.setText("");
        tfIsbn.setText("");
        cbGenero.setValue("<Seleccion>");
        tfTitulo.requestFocus();
    }

}
