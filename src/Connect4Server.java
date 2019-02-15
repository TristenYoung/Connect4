/**Connect4Server handles logic to handle of Connect4 board for independant user session
 *  * @author Tristen Young
 *  * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


public class Connect4Server extends Application
        implements Connect4Constants {
    private int sessionNo = 1; // Number a session
    Map<String, Connect4> current_games = new HashMap<String, Connect4>();
    private static int count = 0;

    /**start method allows running of client logic for a specific thread
     *
     * @param primaryStage  - application visuals
     */
    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        TextArea taLog = new TextArea();

        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
        primaryStage.setTitle("TicTacToeServer"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread( () -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> taLog.appendText(new Date() +
                        ": Server started at socket 8000\n"));

                // Ready to create a session for every two players
                while (true) {
                    int currSessionNo = sessionNo;
                    sessionNo++;

                    Platform.runLater(() -> taLog.appendText(
                            new Date() + ": Waiting for player(s) to join session " + currSessionNo + '\n'));

                    // Connect to player 1
                    Socket player1 = serverSocket.accept();

                    // Notify that the player is Player 1
                    DataInputStream dataInStream = new DataInputStream(player1.getInputStream()); // 1
                    int gameType = dataInStream.readInt(); // 2
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
                    boolean partnered = false;

                    // if loop for Two Player Mode logic
                    if (gameType == 0) {

                        Platform.runLater(() -> {
                            taLog.appendText(new Date() + ": Player 1 joined session " + currSessionNo + '\n');
                            taLog.appendText(
                                    "Player 1's IP address: " + player1.getInetAddress().getHostAddress() + '\n');
                        });

                        while (!partnered) // Partner not already assigned
                        {
                            // Connect to player 2
                            Socket player2 = serverSocket.accept();
                            DataInputStream dataInStream2 = new DataInputStream(player2.getInputStream()); // 1
                            gameType = dataInStream2.readInt(); // 2

                            if (gameType == 0) {
                                Platform.runLater(() -> {
                                    taLog.appendText(new Date() + ": Player 2 joined session " + currSessionNo + '\n');
                                    taLog.appendText("Player 2's IP address: "
                                            + player2.getInetAddress().getHostAddress() + '\n');
                                });

                                // notify that the player is Player 2
                                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                                // display this session and increment session number
                                Platform.runLater(() -> taLog.appendText(
                                        new Date() + ": Start a thread for session " + (sessionNo - 1) + '\n'));

                                Connect4 game = new Connect4();
                                partnered = true;

                                // launch a new thread for this session of two players
                                new Thread(new HandleASession(player1, player2)).start();
                            } else {
                                // notify that the player is Player 1 against AI
                                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER1);
                                int newSession = sessionNo;
                                sessionNo++;

                                Platform.runLater(() -> taLog.appendText(new Date() + " Start a thread for session "
                                        + newSession + " , player1 faces the computer" + '\n'));

                                Connect4 game = new Connect4();
                                Connect4ComputerPlayer comp = new Connect4ComputerPlayer(game);
                                new Thread(new HandleASession(player2, comp)).start();
                            }
                        }
                    } else {
                        Connect4 game = new Connect4();

                        Platform.runLater(() -> taLog.appendText(new Date() + " Start a thread for session "
                                + currSessionNo + " , player1 faces the computer" + '\n'));

                        Connect4ComputerPlayer comp = new Connect4ComputerPlayer(game);
                        new Thread(new HandleASession(player1, comp)).start();
                    }
                }
            } catch (SocketException ex) {
                /*
                 * DO NOTHING
                 *
                 * If a Socket Exception occurs, it means the server window was closed. There's
                 * not really an error, it's just that the server was terminated.
                 */
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**HandleASession is a inner class to
     *
     */

    // Define the thread class for handling a new session for two players
    class HandleASession implements Runnable, Connect4Constants {
        private Socket player1;
        private Object player2;
        private Connect4 game;
        private String arrCount = "" + count;

        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;

        // Continue to play
        private boolean continueToPlay = true;

        /** Construct a thread */
        public HandleASession(Socket player1, Object player2) {
            this.player1 = player1;
            if (player2.getClass() == player1.getClass())
                this.player2 = (Socket) player2;
            else
                this.player2 = (Connect4ComputerPlayer) player2;
            this.game = new Connect4();
            current_games.put("" + count, game);
            count++;

        }

        /**
         * The method run() runs the session on the server, then is idle and pushes
         * players to be either waiting for another player or joining another player.
         */
        public void run() {
            try {
                // initialize all player data streams
                fromPlayer1 = new DataInputStream(player1.getInputStream());
                toPlayer1 = new DataOutputStream(player1.getOutputStream());

                if (player2.getClass() == player1.getClass()) {
                    fromPlayer2 = new DataInputStream(((Socket) player2).getInputStream());
                    toPlayer2 = new DataOutputStream(((Socket) player2).getOutputStream());

                    // notify player one to begin the game
                    toPlayer1.writeInt(1);

                    // Continuously serve the players and determine and report
                    // the game status to the players
                    while (!current_games.get(arrCount).winCondition()) {
                        // Receive a move from player 1
                        boolean valid = false;
                        while(!valid) {
                            try {
                                int column = fromPlayer1.readInt();
                                current_games.get(arrCount).move(column);
                                int row = current_games.get(arrCount).current_row;
                                valid = true;
                                // Check if Player 1 wins
                                if (current_games.get(arrCount).winCondition() && current_games.get(arrCount).player == 'X') {
                                    toPlayer1.writeInt(PLAYER1_WON);
                                    toPlayer2.writeInt(PLAYER1_WON);
                                    sendMove(toPlayer2, row, column);
                                    break; // Break the loop
                                }
                                else {
                                    // Notify player 2 to take the turn
                                    toPlayer2.writeInt(CONTINUE);

                                    // Send player 1's selected row and column to player 2
                                    System.out.println(row);
                                    sendMove(toPlayer2, row, column);
                                }
                            }
                            catch(IllegalRowException e){
                                if (current_games.get(arrCount).player == 'O'){
                                    current_games.get(arrCount).player = 'X';
                                }else{
                                    current_games.get(arrCount).player = 'O';
                                }
                            }
                        }


                        valid = false;
                        while(!valid) {
                            try {
                                int column = fromPlayer2.readInt();
                                current_games.get(arrCount).move(column);
                                int row = current_games.get(arrCount).current_row;

                                valid = true;
                                // Check if Player 2 wins
                                if (current_games.get(arrCount).winCondition() && current_games.get(arrCount).player == 'O') {
                                    toPlayer1.writeInt(PLAYER2_WON);
                                    toPlayer2.writeInt(PLAYER2_WON);
                                    sendMove(toPlayer1, row, column);
                                    break; // Break the loop
                                }
                                else {
                                    // Notify player 1 to take the turn
                                    toPlayer1.writeInt(CONTINUE);

                                    // Send player 2's selected row and column to player 1
                                    System.out.println(row);
                                    sendMove(toPlayer1, row, column);
                                }
                            }
                            catch(IllegalRowException e){
                                if (current_games.get(arrCount).player == 'O'){
                                    current_games.get(arrCount).player = 'X';
                                }else{
                                    current_games.get(arrCount).player = 'O';
                                }
                            }
                        }
                    }
                } else {
                    // notify player one to begin the game
                    toPlayer1.writeInt(1);
                    Connect4ComputerPlayer comp = new Connect4ComputerPlayer(current_games.get(arrCount));
                 //   comp = (Connect4ComputerPlayer) player2;

                    // Continuously serve the players and determine and report
                    // the game status to the players
                    while (!current_games.get(arrCount).winCondition()) {
                        // Receive a move from player 1
                        boolean valid = false;
                        while(!valid) {
                            try {
                                int column = fromPlayer1.readInt();
                                current_games.get(arrCount).move(column);
                                int row = current_games.get(arrCount).current_row;
                                System.out.println(row);
                                System.out.println(current_games.get(arrCount).player);
                                // Check if Player 1 wins
                                if (current_games.get(arrCount).winCondition() && current_games.get(arrCount).player == 'X') {
                                    toPlayer1.writeInt(PLAYER1_WON);
                                    break; // Break the loop
                                }
                                valid = true;
                            }
                            catch(IllegalRowException e){
                                if (current_games.get(arrCount).player == 'O'){
                                    current_games.get(arrCount).player = 'X';
                                }else{
                                    current_games.get(arrCount).player = 'O';
                                }
                            }
                        }


                        valid = false;
                        while(!valid) {
                                comp.computerIntelligence();
                                System.out.println(current_games.get(arrCount));
                                int row = current_games.get(arrCount).current_row;
                                // Check if compmuter wins
                                if (current_games.get(arrCount).winCondition() && current_games.get(arrCount).player == 'O') {
                                    toPlayer1.writeInt(PLAYER2_WON);
                                    break; // Break the loop
                                }
                                else {
                                    // Notify player 1 to take the turn
                                    toPlayer1.writeInt(CONTINUE);

                                    // Send player 2's selected row and column to player 1
                                    System.out.println(row);
                                    sendMove(toPlayer1, row, comp.getColumn());
                                }
                            valid = true;

                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        /** Send the move to other player for session with another player
         * @param out, rowm column*/
        private void sendMove(DataOutputStream out, int row, int column)
                throws IOException {
            out.writeInt(row); // Send row index
            out.writeInt(column); // Send column index
        }
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     * @param args -which  are String[] for launching application
     */
    public static void main(String[] args) {
        launch(args);
    }
}