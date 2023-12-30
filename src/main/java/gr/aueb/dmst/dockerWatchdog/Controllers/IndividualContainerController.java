package gr.aueb.dmst.dockerWatchdog.Controllers;

import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.InstanceScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static gr.aueb.dmst.dockerWatchdog.Application.DesktopApp.client;
import static gr.aueb.dmst.dockerWatchdog.Main.dockerClient;

public class IndividualContainerController {

    @FXML
    private SplitPane infoCard;

    @FXML
    private Text headTextContainer;
    @FXML
    private Label containerIdLabel;
    @FXML
    private Label containerNameLabel;
    @FXML
    private Label containerStatusLabel;
    @FXML
    private Label containerImageLabel;
    @FXML
    private VBox notificationBox;

    @FXML
    TextArea textArea;

    private InstanceScene instanceScene;
    private Stage stage;
    private Parent root;

    public void changeScene(ActionEvent actionEvent, String fxmlFile) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
        stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
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

    public void onInstanceDoubleClick(InstanceScene instance) {
        this.instanceScene = instance;
        headTextContainer.setText("Container: " + instance.getName());
        containerIdLabel.setText("ID: " + instance.getId());
        containerNameLabel.setText("Name: " + instance.getName());
        containerStatusLabel.setText("Status: " + instance.getStatus());
        containerImageLabel.setText("Image: " + instance.getImage());

        // Specify container ID or name
        String containerId = instance.getId();

        // Execute the command and update the TextArea with each log frame
        Main.dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        // Process each log frame
                        String logLine = item.toString();

                        // Update the TextArea on the JavaFX Application Thread
                        javafx.application.Platform.runLater(() -> {
                            textArea.appendText(logLine + "\n");
                        });
                    }
                });
        infoCard.setVisible(true);
    }

    public void removeContainer(ActionEvent actionEvent) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/delete"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        changeScene(actionEvent, "containersScene.fxml");
    }

    public void pauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/pause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has pause.");
        }

        this.instanceScene.setStatus("Paused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void unpauseContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/unpause"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has unpause.");
        }

        this.instanceScene.setStatus("Unpaused");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void renameContainer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Container");
        dialog.setHeaderText("Enter the new name for the container:");
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName == null || newName.trim().isEmpty()) {
                return;
            }

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/rename?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8)))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    showNotification("Container Event", "Container " + this.instanceScene.getName() + " has renamed to " + newName + ".");
                }

                containerNameLabel.setText("Name: " + newName);
                this.instanceScene.setName(newName);
                headTextContainer.setText("Container: " + newName);
            } catch (IOException | InterruptedException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public void startContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/start"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has started.");
        }

        this.instanceScene.setStatus("running");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void stopContainer() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/stop"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has stopped.");
        }

        this.instanceScene.setStatus("exited");
        containerStatusLabel.setText("Status: " + this.instanceScene.getStatus());
    }

    public void restartContainer() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Restarting the container with ID " + this.instanceScene.getId() + "...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/containers/" + this.instanceScene.getId() + "/restart"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            showNotification("Container Event", "Container " + this.instanceScene.getName() + " has restarted.");
        }

    }

    public void showNotification(String title, String content) {
        Platform.runLater(() -> {
            // Create a Popup
            Popup notification = new Popup();

            // Create a Label for the title and content
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
            Label contentLabel = new Label(content);
            contentLabel.setTextFill(Color.WHITE);

            // Add the Labels to a VBox
            VBox box = new VBox(titleLabel, contentLabel);
            box.setStyle("-fx-background-color: #4272F1; -fx-padding: 10px; -fx-border-color: black; -fx-border-width: 1px;");

            // Add the VBox to the Popup
            notification.getContent().add(box);

            // Get the screen coordinates of the VBox
            Point2D point = notificationBox.localToScreen(notificationBox.getWidth() - box.getWidth(), notificationBox.getHeight() - box.getHeight());

            // Show the Popup at the specified position
            notification.show(notificationBox.getScene().getWindow(), point.getX(), point.getY());

            // Set a timeline to hide the Popup after 3 seconds
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), evt -> notification.hide()));
            timeline.play();
        });
    }
}