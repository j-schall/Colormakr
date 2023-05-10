package com.colormakr;

import com.colormakr.database.SQLiteJDBC;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private ColorPicker colorPicker;

    @FXML
    private TextField txtFieldHex;

    @FXML
    private TextField txtFieldRGB;

    @FXML
    private AnchorPane colorDisplay;
    @FXML
    private Button sButton;
    @FXML
    private Button saveButton;
    @FXML
    private AnchorPane savedColor1;
    @FXML
    private AnchorPane savedColor2;
    @FXML
    private AnchorPane savedColor3;
    @FXML
    private AnchorPane savedColor4;
    @FXML
    private AnchorPane savedColor5;

    private Scene scene;
    private int clickCount = 0;
    private Color color = null;
    private Connection mainConn = null;
    private Statement mainStmt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            queryColor();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        colorPicker.setOnAction(event -> {
            String hex = "#" + colorPicker.getValue().toString().substring(2, 8);
            txtFieldHex.setText(hex);
            txtFieldRGB.setText(hexToRGB(hex));
        });

        try {
            getMouseColor();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private String hexToRGB(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5), 16);
        return String.format("%d, %d, %d", r, g, b);
    }

    private void getMouseColor() throws AWTException {
        Time millisStart = new Time(System.currentTimeMillis());
        String finalHex = null;

        sButton.setOnMouseClicked(MouseEvent -> {
            sButton.setText("Stop");

            // Timeline erstellen, um die Anwendung später nicht zu blockieren und zu aktualisieren
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {

                // Die Position, des Cursor berechnen und die Farbe erkennen
                PointerInfo mouseInfo = MouseInfo.getPointerInfo();
                Point pos = mouseInfo.getLocation();
                int x = (int) pos.getX();
                int y = (int) pos.getY();
                Robot robot = null;

                try {
                    robot = new Robot();
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
                robot.mouseMove(x, y);
                color = robot.getPixelColor(x, y);

                int r = color.getRed();
                int b = color.getBlue();
                int g = color.getGreen();

                String hex = String.format("#%02x%02x%02x", r, g, b);

                // Aktualisierung der GUI, damit die Farbe angezeigt wird, worüber der Cursor geht
                Platform.runLater(() -> {
                    colorDisplay.setStyle("-fx-background-color: " + hex + ";");
                    txtFieldHex.setText(hex);
                    txtFieldRGB.setText(hexToRGB(hex));
                    colorPicker.setValue(javafx.scene.paint.Color.valueOf(hex));
                });
                e.consume();
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            Time millisStop = new Time(System.currentTimeMillis());

            // Abstand wird ausgerechnet, wie lange der Button nicht mehr gedrückt wurde
            long millis = millisStop.getTime() - millisStart.getTime();

            // Wenn der SuchButton gedrückt wird, wird die Farbe die in Hex-Dezimal Form steht, in die Datenbank gespeichert
            sButton.setOnMouseClicked(event -> {
                // Solange der Button nicht nochmal gedrückt wurde, soll das Programm nicht weiterlaufen
                clickCount++;
                if (clickCount == 1) {
                    sButton.setText("Search");
                    timeline.stop();
                } else if (clickCount == 2) {
                    sButton.setText("Stop");
                    timeline.play();
                    clickCount = 0;
                }

            });

            // Wenn ALT gedrückt wird soll das selbe wie, wenn der sButton gedrückt wurde, passieren
            scene = Main.mainScene;
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ALT) {
                    clickCount++;
                    if (clickCount == 1) {
                        sButton.setText("Search");
                        timeline.stop();
                    } else if (clickCount == 2) {
                        sButton.setText("Stop");
                        timeline.play();
                        clickCount = 0;
                    }
                }
            });
        });
    }

    @FXML
    void saveInDB(ActionEvent event) throws SQLException {
        String hex = txtFieldHex.getText();
        saveColor(hex);
    }

    private void saveColor(String hex) {
        try {
            mainConn = SQLiteJDBC.connection();
            mainStmt = mainConn.createStatement();

            String cmd = "INSERT INTO colors (Hex) VALUES ('" + hex + "')";
            mainStmt.executeUpdate(cmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void queryColor() throws SQLException {
        try {
            mainConn = SQLiteJDBC.connection();
            mainStmt = mainConn.createStatement();

            String queryCmd = "SELECT * FROM colors";
            ResultSet rSet = mainStmt.executeQuery(queryCmd);
            AnchorPane[] savedColors = {savedColor1, savedColor2, savedColor3, savedColor4, savedColor5};

            // Datenbank wird ausgelesen und die Daten in Hex-Form werden den Anchorpanes "savedColor", als Hintergrund festgelegt, damit man die letzten fünf Farben auswählen kann.
            while (rSet.next()) {
                String color = rSet.getString(1);

                if (savedColor1.getStyle().equals("")) {
                    savedColor1.setStyle("-fx-background-color: " + color + ";");
                } else if (savedColor2.getStyle().equals("")) {
                    savedColor2.setStyle("-fx-background-color: " + color + ";");
                } else if (savedColor3.getStyle().equals("")) {
                    savedColor3.setStyle("-fx-background-color: " + color + ";");
                } else if (savedColor4.getStyle().equals("")) {
                    savedColor4.setStyle("-fx-background-color: " + color + ";");
                } else if (savedColor5.getStyle().equals("")) {
                    savedColor5.setStyle("-fx-background-color: " + color + ";");
                }

                AnchorPane[] panes = {savedColor1, savedColor2, savedColor3, savedColor4, savedColor5};

                for (int i )

                /*
                Timeline line = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                    String hex = txtFieldHex.getText();
                    saveButton.setOnMouseClicked(event -> {
                        Platform.runLater(() -> {
                            saveColor(hex);
                        });

                    });
                }));
                line.setCycleCount(Timeline.INDEFINITE);
                line.play();
                break;
                 */
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteColor(String hex) {
        try {
            mainConn = SQLiteJDBC.connection();
            mainStmt = mainConn.createStatement();

            String cmd = "DELETE FROM colors WHERE (Hex) = ('" + hex + "')";
            mainStmt.executeUpdate(cmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
