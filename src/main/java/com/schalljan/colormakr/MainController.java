package com.schalljan.colormakr;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

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
    private java.awt.Color color = null;
    private ArrayList<com.schalljan.colormakr.Color> colorCodes;
    private final String FS = FileSystems.getDefault().getSeparator();
    private final String HOME_DIR = System.getProperty("user.home");
    private final String DIRECTORY = HOME_DIR + FS + ".colormakr" + FS + "colors.json";
    private AnchorPane[] PANES;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorCodes = new ArrayList<>();
        PANES = new AnchorPane[]{savedColor1, savedColor2, savedColor3, savedColor4, savedColor5};
        showColors();
        colorPicker.setOnAction(event -> {
            String hex = "#" + colorPicker.getValue().toString().substring(2, 8);
            txtFieldHex.setText(hex);
            txtFieldRGB.setText(Color.hexToRGB(hex));
        });
        getMouseColor();
    }

    private void getMouseColor() {
        sButton.setOnMouseClicked(MouseEvent -> {
            sButton.setText("Stop");

            // Timeline erstellen, um die Anwendung später nicht zu blockieren und zu aktualisieren
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {

                // Die Position, des Cursor berechnen und die Farbe erkennen
                PointerInfo mouseInfo = MouseInfo.getPointerInfo();
                Point pos = mouseInfo.getLocation();
                int x = (int) pos.getX();
                int y = (int) pos.getY();
                Robot robot;

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
                    txtFieldRGB.setText(Color.hexToRGB(hex));
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
    void saveInDB() {
        // If the saveButton is pressed than the color will be saved in the database
        String hex = txtFieldHex.getText();

        Platform.runLater(() -> {
            saveColor(hex);
            showColors();
        });
    }

    private void saveColor(String hex) {
        colorCodes = (ArrayList<Color>) deserializeJsonFile();
        colorCodes.add(new Color(hex));
        String json = new Gson().toJson(colorCodes);
        try (FileWriter jsonFile = new FileWriter(DIRECTORY)) {
            Path path = Path.of(DIRECTORY);
            if (!Files.isWritable(path))
                throw new RuntimeException("The current directory is read-only. Please check the permissions for this directory.");
            jsonFile.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String queryColors() {
        try {
            Path jsonPath = Path.of(DIRECTORY);
            StringBuilder jsonContent = new StringBuilder();
            Scanner scanner = new Scanner(new File(jsonPath.toUri()));
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine());
            }
            return jsonContent.toString();
        } catch (NullPointerException | FileNotFoundException e) {
            File newDir = new File(HOME_DIR + FS + ".colormakr");
            if (!newDir.exists())
                newDir.mkdirs();
            return null;
        }
    }

    private void deleteOldColors(List<Color> newColors) {
        List<Color> colors = deserializeJsonFile();
        if (colors.isEmpty())
            return;

        // Remove elements which aren't used anymore in the color hot bar of the application
        colors = colors.subList(colors.indexOf(newColors.get(0)), colors.size());
        String json = new Gson().toJson(colors);
        try (FileWriter writer = new FileWriter(DIRECTORY)) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Color> deserializeJsonFile() {
        String json = queryColors();
        if (json == null || json.isEmpty())
            return new ArrayList<>();
        return new Gson().fromJson(json, new TypeToken<List<Color>>() {
        }.getType());
    }

    private void showColors() {
        List<Color> colors = deserializeJsonFile();
        if (colors.isEmpty())
            return;
        int lastElementIndex = colors.indexOf(colors.get(colors.size() - 1));
        int panesLength = PANES.length - 1;

        if (lastElementIndex > panesLength) {
            int diff = Math.abs(panesLength - lastElementIndex);
            colors.subList(0, diff).clear();
            deleteOldColors(colors);
        }

        for (int i = 0; i < colors.size(); i++) {
            com.schalljan.colormakr.Color c = colors.get(i);
            PANES[i].setStyle("-fx-background-color: " + c.getColor() + ";");
            PANES[i].setOnMouseClicked(mouseEvent -> {
                String currColor = c.getColor();
                txtFieldHex.setText(currColor);
                txtFieldRGB.setText(com.schalljan.colormakr.Color.hexToRGB(currColor));
            });
        }
    }
}