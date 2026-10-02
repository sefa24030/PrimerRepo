package fia.eisi.poo.gestioncarreras.controller;

import fia.eisi.poo.gestioncarreras.model.Carrera;
import fia.eisi.poo.gestioncarreras.util.Datos;
import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConsultarCarrerasController {

    @FXML private TextField buscarField;
    @FXML private TableView<Carrera> tablaCarreras;
    @FXML private TableColumn<Carrera, String> colCodigo;
    @FXML private TableColumn<Carrera, String> colNombre;
    @FXML private TableColumn<Carrera, String> colTitulo;
    @FXML private TableColumn<Carrera, String> colFacultad;
    @FXML private Label lblInfo;

    private ObservableList<Carrera> datosCompletos;
    private ObservableList<Carrera> datosFiltrados;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        colFacultad.setCellValueFactory(cellData -> {
            Carrera c = cellData.getValue();
            String texto = (c.getFacultad() != null) ? c.getFacultad().getNombre() : "";
            return new SimpleStringProperty(texto);
        });

        // Se obtienen todas las carreras
        datosCompletos = Datos.getCarreras();

        // Segunda lista para filtrar; al inicio tiene todas las carreras
        datosFiltrados = FXCollections.observableArrayList();
        datosFiltrados.setAll(datosCompletos);

        // La tabla se actualiza sola cuando cambia datosFiltrados
        tablaCarreras.setItems(datosFiltrados);
        actualizarInfo();

        // Doble clic abre la ventana modal de mantenimiento
        tablaCarreras.setRowFactory(tv -> {
            TableRow<Carrera> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirMantenimiento(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void filtrar() {
        String texto = buscarField.getText();

        datosFiltrados.clear();

        if (texto == null || texto.trim().isEmpty()) {
            datosFiltrados.setAll(datosCompletos);
            actualizarInfo();
            return;
        }

        String t = texto.toLowerCase().trim();
        for (Carrera c : datosCompletos) {
            if (c.getCodigo().toLowerCase().contains(t)
                    || c.getNombre().toLowerCase().contains(t)) {
                datosFiltrados.add(c);
            }
        }
        actualizarInfo();
    }

    @FXML
    private void nuevaCarrera() {
        abrirMantenimiento(null);
    }

    @FXML
    private void limpiarFiltro() {
        buscarField.clear();
        tablaCarreras.getSelectionModel().clearSelection();
        filtrar();
    }

    @FXML
    private void salir() {
        ((Stage) buscarField.getScene().getWindow()).close();
    }

    private void abrirMantenimiento(Carrera carrera) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fia/eisi/poo/gestioncarreras/view/MantenimientoCarrera.fxml"));
            Parent root = loader.load();

            MantenimientoCarreraController controller = loader.getController();
            controller.setCarrera(carrera);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tablaCarreras.getScene().getWindow());
            stage.setTitle(carrera == null ? "Nueva cosa xd" : "Editar Carrera");
            stage.showAndWait();

            // Al cerrarse la ventana, se resincroniza la tabla
            filtrar();
            tablaCarreras.refresh();
        } catch (IOException | NullPointerException e) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo abrir la ventana de mantenimiento: " + e.getMessage()).showAndWait();
        }
    }

    private void actualizarInfo() {
        if (lblInfo != null) {
            lblInfo.setText("Mostrando " + datosFiltrados.size()
                    + " de " + datosCompletos.size() + " carreras");
        }
    }
}