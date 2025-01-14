package tictactoeserver.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import tictactoeserver.model.User;
import tictactoeserver.model.UserDao;
import tictactoeserver.server.Server;
import tictactoeserver.view.DashBoard;
import tictactoeserver.server.handler.ClientHandler;

public class DashBoardController extends DashBoard {

    private Server server;
    private Thread serverThread;
    private Thread updateChart;
    private boolean running;
    private static ArrayList<User> availableUsers = new ArrayList();
    UserDao uDao;

    public DashBoardController(Stage stage) {
        uDao = UserDao.getInstance();
        btnController.setOnAction(e -> {
            if (!running) {
                startServer();
            } else {
                stopServer();
                stopBarChart();
            }
        });

        stage.setOnCloseRequest(e -> {
            if (running) {
                stopServer();
                stopBarChart();
            }
        });
    }

    private void startServer() {
        running = true;
        serverThread = new Thread(() -> {
            server = new Server();
            server.start();
        });
        serverThread.start();

        Platform.runLater(() -> {
            btnController.setText("Stop Server");
            btnController.setStyle("-fx-background-color: red;-fx-text-fill: white;");
        });
        startBarchart();

    }

    private void stopServer() {
        running = false;
        if (serverThread != null && serverThread.isAlive()) {
            server.stop();
            serverThread.stop();
        }

        Platform.runLater(() -> {
            btnController.setText("Restart Server");
            btnController.setStyle("-fx-background-color: orange;-fx-text-fill: white;");
        });
    }

    private void startBarchart() {
        int[] ar = {0, 0};
        updateChart = new Thread(() -> {
            while (true) {
                try {
                    availableUsers = uDao.getAvailableUsers();
                    ClientHandler.getAvailableUsers();
                    ar[0] = UserDao.getOnlineUsers()[0];
                    ar[1] = UserDao.getOnlineUsers()[1];
                    Platform.runLater(() -> {
                        updateGraph(ar[0], ar[1]);
                    });
                    Thread.sleep(3000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(DashBoardController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DashBoardController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
        updateChart.start();
    }

    public void updateGraph(int online, int offline) {

        dataSeries1.getData().add(new XYChart.Data("Online", online));
        dataSeries1.getData().add(new XYChart.Data("Offline", offline));
    }

    private void stopBarChart() {
        updateChart.stop();
        server.stop();
        Platform.runLater(() -> {
            updateGraph(0, 0);
        });
    }

    public static ArrayList<User> getAvailableUsers() {
        return availableUsers;
    }

}
