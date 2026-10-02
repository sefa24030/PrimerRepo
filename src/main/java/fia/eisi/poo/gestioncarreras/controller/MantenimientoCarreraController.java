/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package fia.eisi.poo.gestioncarreras.controller;

import fia.eisi.poo.gestioncarreras.model.Carrera;
import fia.eisi.poo.gestioncarreras.model.Facultad;
import fia.eisi.poo.gestioncarreras.util.Datos;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Rudy
 */
public class MantenimientoCarreraController {

    @FXML
    private TextField codigoField;
    @FXML
    private TextField nombreField;
    @FXML
    private TextField tituloField;
    @FXML
    private ComboBox<Facultad> facultadCombo;
    @FXML
    private Label errorLabel;
    @FXML
    private Label tituloVentana;
    @FXML
    private Button eliminarButton;

    private Carrera carrera;       // null = insertar, no null = editar
    private Runnable onClose;

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

   public void setCarrera(Carrera carrera) {
    this.carrera = carrera;

    if (carrera == null) {
        tituloVentana.setText("Nueva Carrera");
        eliminarButton.setVisible(false);
        eliminarButton.setManaged(false);
    } else {
        tituloVentana.setText("Editar Carrera");
        codigoField.setText(carrera.getCodigo());
        codigoField.setDisable(true);   // el código no se edita
        nombreField.setText(carrera.getNombre());
        tituloField.setText(carrera.getTitulo());
        facultadCombo.getSelectionModel().select(carrera.getFacultad());
    }
}

@FXML
public void initialize() {
    facultadCombo.setItems(Datos.getFacultades());
    facultadCombo.setConverter(new StringConverter<>() {
        @Override
        public String toString(Facultad f) {
            return f == null ? "" : f.toString();
        }

        @Override
        public Facultad fromString(String s) {
            return null;
        }
    });
    errorLabel.setText("");
}

@FXML
private void guardar() {
    String codigo = codigoField.getText().trim();
    String nombre = nombreField.getText().trim();
    String titulo = tituloField.getText().trim();
    Facultad facultad = facultadCombo.getValue();

    if (codigo.isEmpty() || nombre.isEmpty() || titulo.isEmpty() || facultad == null) {
        errorLabel.setText("Todos los campos son obligatorios.");
        return;
    }

    if (carrera == null) {
        // Nueva carrera: el código no puede repetirse
        boolean error = false;
        for (Carrera c : Datos.getCarreras()) {
            if (c.getCodigo().equalsIgnoreCase(codigo)) {
                error = true;
                break;
            }
        }

        if (error) {
            errorLabel.setText("Ya existe una carrera con ese código.");
            return;
        }

        Carrera nuevaCarrera = new Carrera(codigo, nombre, titulo, facultad);
        Datos.agregarCarrera(nuevaCarrera);
    } else {
        // Edición
        Facultad anterior = carrera.getFacultad();
        carrera.setNombre(nombre);
        carrera.setTitulo(titulo);
        carrera.setFacultad(facultad);

        // Si cambió de facultad, se actualizan las listas de cada facultad
        if (anterior != facultad) {
            if (anterior != null) {
                anterior.getCarreras().remove(carrera);
            }
            facultad.getCarreras().add(carrera);
        }
    }

    cerrar();
}

@FXML
private void eliminar() {
    if (carrera == null) {
        return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Está seguro de eliminar la carrera \"" + carrera.getNombre() + "\"?",
            ButtonType.YES, ButtonType.NO);
    confirm.setTitle("Confirmar eliminación");
    confirm.setHeaderText(null);
    confirm.showAndWait().ifPresent(bt -> {
        if (bt == ButtonType.YES) {
            Datos.eliminarCarrera(carrera);
            cerrar();
        }
    });
}

@FXML
private void cancelar() {
    cerrar();
}

private void cerrar() {
    if (onClose != null) {
        onClose.run();
    }
    ((Stage) codigoField.getScene().getWindow()).close();
}

}
