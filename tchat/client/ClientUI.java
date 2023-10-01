package univ.nc.fx.network.tcp.tchat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientUI extends Application implements EventHandler<ActionEvent>, Runnable {

    private TextField ip;
    private TextField port;
    private TextField nickname;
    private Button connect;
    private Button disconnect;
    private TextArea textArea;
    private TextField input;
    private Label status;

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private boolean running = false;

    public void start(Stage stage) throws Exception {

        // Border pane et scene
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        // Zone haute pour la connexion
        ToolBar toolBar = new ToolBar();
        ip = new TextField("127.0.0.1");
        port = new TextField("1234");
        nickname = new TextField("user" + (int) (Math.random() * 100));
        connect = new Button("Connect");
        connect.setOnAction(this);
        disconnect = new Button("Disconnect");
        disconnect.setOnAction(this);
        toolBar.getItems().addAll(ip, port, nickname, connect, disconnect);
        borderPane.setTop(toolBar);

        // Zone centrale de log de tchat
        textArea = new TextArea();
        borderPane.setCenter(textArea);

        // Zone basse pour la zone de texte et le statut
        VBox bottomBox = new VBox();
        status = new Label("Ready");
        input = new TextField();
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && input.getText().trim().length() > 0) {
                sendMessage(nickname.getText() + ": " + input.getText());
                input.setText("");
            }
        });
        bottomBox.getChildren().addAll(input, status);
        borderPane.setBottom(bottomBox);

        // Statut initial déconnecté
        setDisconnectedState();

        stage.setTitle("Chat Client");
        stage.show();
    }

    public void setDisconnectedState() {
        ip.setDisable(false);
        port.setDisable(false);
        nickname.setDisable(false);
        connect.setDisable(false);
        disconnect.setDisable(true);
        input.setDisable(true);
    }

    public void setConnectedState() {
        ip.setDisable(true);
        port.setDisable(true);
        nickname.setDisable(true);
        connect.setDisable(true);
        disconnect.setDisable(false);
        input.setDisable(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void appendMessage(String message) {
        textArea.appendText(message + "\n");
    }

    public void setStatus(String message) {
        status.setText(message);
    }

    public void connectToServer() {
        if (ip.getText().trim().isEmpty() || port.getText().trim().isEmpty() || nickname.getText().trim().isEmpty()) {
            setStatus("Please enter valid IP, port, and nickname.");
            return;
        }

        try {
            socket = new Socket(ip.getText(), Integer.parseInt(port.getText()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter.write(nickname.getText());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Start a separate thread to listen for incoming messages from the server
            new Thread(this).start();

            setStatus("Connected to the server.");
            setConnectedState();
        } catch (IOException e) {
            setStatus("Error connecting to the server.");
        }
    }

    public void disconnectFromServer() {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setDisconnectedState();
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            setStatus("Error sending message.");
        }
    }

    public void run() {
        String message;

        try {
            while ((message = bufferedReader.readLine()) != null) {
                appendMessage(message);
            }
        } catch (IOException e) {
            // Connection closed
            Platform.runLater(() -> setStatus("Disconnected from the server."));
            setDisconnectedState();
        }
    }

    public void handle(ActionEvent event) {
        if (event.getSource() == connect) {
            connectToServer();
        } else if (event.getSource() == disconnect) {
            disconnectFromServer();
        }
    }

    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && input.getText().trim().length() > 0) {
            sendMessage(nickname.getText() + ": " + input.getText());
            input.setText("");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
