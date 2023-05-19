package com.colormakr;

import com.colormakr.database.JDBC;
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
    private int currentIndex = 0;
    private AnchorPane[] panes = {savedColor1, savedColor2, savedColor3, savedColor4, savedColor5};

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

            // Wenn der SuchButton gedrückt wird, wird die Farbe die in Hex-Dezimal Form steht, in die Datenbank gespeichert
            sButton.setOnMouseClicked(event -> {
                // As long as the button was not pressed again, the program should not continue
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

            // When ALT is pressed, the same should happen as when the sButton was pressed.
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
        try {
            saveColor(hex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveColor(String hex) throws SQLException {
        Statement stmt = null;
        try {
            mainConn = JDBC.connection();
            mainConn.setAutoCommit(false);

            stmt = mainConn.createStatement();
            stmt.executeUpdate("INSERT INTO color (Hex) VALUES ('" + hex + "')");
            mainConn.commit();
        } catch (SQLException e) {
            if (mainConn != null) {
                try {
                    mainConn.rollback();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        if (mainConn != null) {
            mainConn.setAutoCommit(true);
        }
    }

    public void queryColor() throws SQLException {
        try {
            mainConn = JDBC.connection();
            mainStmt = mainConn.createStatement();

            String queryCmd = "SELECT * FROM color";
            ResultSet rSet = mainStmt.executeQuery(queryCmd);

            // Database is read and the data in hex form are set to the anchorpanes "savedColor",
            // as background, so that one can select the last five colors
            while (rSet.next()) {
                ResultSet rs = mainStmt.executeQuery("SELECT * FROM color");

                int i = 1;
                int count = 1;
                while (rs.next()) {
                    String hex = rs.getString(1);

                    switch (i) {
                        case 1:
                            savedColor1.setStyle("-fx-background-color: " + hex + ";");
                            savedColor1.setOnMouseClicked(a -> {
                                txtFieldHex.setText(hex);
                                txtFieldRGB.setText(hexToRGB(hex));
                            });
                            count++;
                            break;
                        case 2:
                            savedColor2.setStyle("-fx-background-color: " + hex + ";");
                            savedColor2.setOnMouseClicked(a -> {
                                txtFieldHex.setText(hex);
                                txtFieldRGB.setText(hexToRGB(hex));
                            });
                            count++;
                            break;
                        case 3:
                            savedColor3.setStyle("-fx-background-color: " + hex + ";");
                            savedColor3.setOnMouseClicked(a -> {
                                txtFieldHex.setText(hex);
                                txtFieldRGB.setText(hexToRGB(hex));
                            });
                            count++;
                            break;
                        case 4:
                            savedColor4.setStyle("-fx-background-color: " + hex + ";");
                            savedColor4.setOnMouseClicked(a -> {
                                txtFieldHex.setText(hex);
                                txtFieldRGB.setText(hexToRGB(hex));
                            });
                            count++;
                            break;
                        case 5:
                            savedColor5.setStyle("-fx-background-color: " + hex + ";");
                            savedColor5.setOnMouseClicked(a -> {
                                txtFieldHex.setText(hex);
                                txtFieldRGB.setText(hexToRGB(hex));
                            });
                            count++;
                            break;
                    }
                    i++;
                }

                int finalCount = count;
                Timeline line = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                    String hex = txtFieldHex.getText();
                    if (currentIndex < panes.length && saveButton.isPressed()) {
                        try {
                            saveColor(hex);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        if (finalCount > 5) {
                            try {
                                deleteRows();
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            panes[currentIndex].setStyle("-fx-background-color: " + hex + ";");
                            currentIndex++;
                        }

                    }
                }));
                line.setCycleCount(Timeline.INDEFINITE);
                line.play();
                break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This method will be called, when all AnchorPanes with the saved colors have already a background color,
    // so they will be deleted from the database
    private void deleteRows() throws SQLException {
        Statement stmt = null;
        try {
            mainConn = JDBC.connection();
            mainConn.setAutoCommit(false);

            stmt = mainConn.createStatement();
            stmt.executeUpdate("DELETE FROM color");

            mainConn.commit();
        } catch (SQLException e) {
            if (mainConn != null) {
                try {
                    mainConn.rollback();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        if (mainConn != null) {
            mainConn.setAutoCommit(true);
        }
    }
}
