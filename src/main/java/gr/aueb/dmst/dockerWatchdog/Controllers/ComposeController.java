package gr.aueb.dmst.dockerWatchdog.Controllers;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.yaml.snakeyaml.Yaml;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;

public class ComposeController {

    @FXML
    TextArea yamlContentArea;

    @FXML
    public ImageView watchdogImage;
    private Parent root;

    private String yamlFilePath;
    boolean isShowingConfig = false;

    @FXML
    Button backButton;
    @FXML
    Button showConfigButton;
    @FXML
    Button validateButton;
    @FXML
    private Label fileNameLabel;

    @FXML
    Label savedLabel;

    public void initialize() {
        yamlContentArea.textProperty().addListener((observable, oldValue, newValue) -> savedLabel.setText("Unsaved"));
        Image img = new Image(getClass().getResource("/images/back.png").toExternalForm());
        ImageView view = new ImageView(img);
        view.setFitHeight(20);
        view.setPreserveRatio(true);
        Image imgHover = new Image(getClass().getResource("/images/backHover.png").toExternalForm());
        backButton.setGraphic(view);

        backButton.setOnMouseEntered(event -> {
            // Change image on hover
            view.setImage(imgHover);
            view.setOpacity(0.8);
        });

        backButton.setOnMouseExited(event -> {
            // Change back to default image when not hovered
            view.setImage(img);
            view.setOpacity(1);
        });

        // Install funny tooltip on watchdog imageView
        Tooltip woof = new Tooltip("Woof!");
        woof.setShowDelay(Duration.millis(20));
        Tooltip.install(watchdogImage,woof);
    }

    private void loadYamlFile() {
        try {
            String yamlContent = new String(Files.readAllBytes(Paths.get(yamlFilePath)));
            yamlContentArea.setText(yamlContent);
            String fileName = Paths.get(yamlFilePath).getFileName().toString();
            fileNameLabel.setText(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/" + fxmlFile)));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void changeToContainersScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "containersScene.fxml");
    }

    public void changeToImagesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "imagesScene.fxml");
    }

    public void changeToVolumesScene(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/volumesScene.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VolumesController volumesController = loader.getController();
        volumesController.refreshVolumes();
        changeScene(actionEvent, "volumesScene.fxml");
    }

    public void changeToGraphicsScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "graphicsScene.fxml");
    }
    public void changeToKubernetesScene(ActionEvent actionEvent) throws IOException {
        changeScene(actionEvent, "kubernetesScene.fxml");
    }

    public void setYamlFilePath(String yamlFilePath) {
        this.yamlFilePath = yamlFilePath;
        loadYamlFile();
    }

    public void startDockerCompose() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f", yamlFilePath, "up", "-d");

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Docker Compose file ran successfully");
        } else {
            System.out.println("Error running Docker Compose file");
        }
    }

    public void stopDockerCompose() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f", yamlFilePath, "stop");

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Docker Compose file stopped successfully");
        } else {
            System.out.println("Error stopping Docker Compose file");
        }
    }

    public void validateYaml() {
        try {
            Yaml yaml = new Yaml();
            yaml.load(new FileInputStream(yamlFilePath));
            validateButton.setText("Valid");
            validateButton.setStyle("-fx-text-fill: white; -fx-background-color: #93C572;");
            validateButton.setDisable(true);
        } catch (Exception e) {
            validateButton.setText("Sorry not valid");
            validateButton.setStyle("-fx-text-fill: white; -fx-background-color: #EC5F47;");
            validateButton.setDisable(true);
        }
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            validateButton.setStyle(null);
            validateButton.setDisable(false);
            validateButton.setText("Validate");
        });
        pause.play();
    }

    public void saveYaml() {
        try {
            String yamlContent = yamlContentArea.getText();
            Files.write(Paths.get(yamlFilePath), yamlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            savedLabel.setText("Saved");
        } catch (Exception e) {
            System.out.println("Error saving YAML file: " + e.getMessage());
        }
    }

    public void showConfig() {
        if (!isShowingConfig) {
            try {
                Yaml yaml = new Yaml();
                Map<String, Object> yamlMap = yaml.load(new FileInputStream(yamlFilePath));
                yamlContentArea.clear();
                yamlContentArea.appendText("Config:\n");
                for (Map.Entry<String, Object> entry : yamlMap.entrySet()) {
                    yamlContentArea.appendText(entry.getKey() + ": " + entry.getValue().toString() + "\n");
                }
                isShowingConfig = true;
                showConfigButton.setText("Show YAML");
            } catch (Exception e) {
                System.out.println("Error reading YAML file: " + e.getMessage());
            }
        } else {
            loadYamlFile();
            isShowingConfig = false;
            showConfigButton.setText("Show Config");
        }
    }
}