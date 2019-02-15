/**Connect4Client creates a display of Connect4 game board and talks to server for the logic
 * allowing pvp or pve in users preferred ui which is either console or gui
 *
 * @author Tristen Young
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.util.InputMismatchException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Connect4Client extends Application implements Connect4Constants {
    // Indicate whether the player has the turn
    private boolean myTurn = false;

    private boolean playgGui = true;
    private int gameType = 0;

    // Indicate the token for the player
    private char myToken = ' ';

    // Indicate the token for the other player
    private char otherToken = ' ';

    // Create and initialize cells
    private Cell[][] cell =  new Cell[7][8];

    // Create and initialize a title label
    private Label lblTitle = new Label();

    // Create and initialize a status label
    private Label lblStatus = new Label();

    // Indicate selected row and column by the current move
    private int columnSelected;

    // Input and output streams from/to server
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // Continue to play?
    private boolean continueToPlay = true;

    // Wait for the player to mark a cell
    private boolean waiting = true;

    private Stage stage;

    // Host name or ip
    private String host = "localhost";

    /**start method vizualies repersentation of current game board
     *
     * @param primaryStage
     */

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        this.stage = stage;
        settings();
    }

    public void displayMain(Stage primaryStage){

        Stage primaryStage1 = new Stage();
        // Pane to hold cell
        GridPane pane = new GridPane();
        for (int i = 1; i < 7; i++)
            for (int j = 1; j < 8; j++)
                pane.add(cell[i][j] = new Cell(i, j), j, i);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(lblTitle);
        borderPane.setCenter(pane);
        borderPane.setBottom(lblStatus);

        // Create a scene and place it in the stage
        Scene scene = new Scene(borderPane, 320, 350);
        primaryStage1.setTitle("TicTacToeClient"); // Set the stage title
        primaryStage1.setScene(scene); // Place the scene in the stage
        primaryStage1.show(); // Display the stage
        display(cell);

        // Connect to the server
        connectToServer();
    }

    /**ConnectToServer  communicates with Connect4Server to check with connect$ game logiv
     *
     *
     */

    private void connectToServer() {
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket(host, 8000);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
            toServer.writeInt(gameType);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        // Control the game on a separate thread
        new Thread(() -> {

            try {
                // Get notification from the server
                int player = fromServer.readInt();

                // Am I player 1 or 2?
                if (player == PLAYER1) {
                    myToken = 'X';
                    otherToken = 'O';
                    Platform.runLater(() -> {
                        lblTitle.setText("Player 1 with token 'X'");
                        lblStatus.setText("Waiting for player 2 to join");
                    });

                    // Receive startup notification from the server
                    fromServer.readInt(); // Whatever read is ignored

                    // The other player has joined
                    Platform.runLater(() ->
                            lblStatus.setText("Player 2 has joined. I start first"));

                    // It is my turn
                    myTurn = true;
                }
                else if (player == PLAYER2) {
                    myToken = 'O';
                    otherToken = 'X';
                    Platform.runLater(() -> {
                        lblTitle.setText("Player 2 with token 'O'");
                        lblStatus.setText("Waiting for player 1 to move");
                    });
                }

                // Continue to play
                while (continueToPlay) {
                    if (player == PLAYER1) {
                        waitForPlayerAction(); // Wait for player 1 to move
                        sendMove(); // Send the move to the server
                        receiveInfoFromServer(); // Receive info from the server
                    }
                    else if (player == PLAYER2) {
                        receiveInfoFromServer(); // Receive info from the server
                        waitForPlayerAction(); // Wait for player 2 to move
                        sendMove(); // Send player 2's move to the server
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**Displays the setting options for Connect 4 game gui
     */
    public void settings(){
        Stage settings = new Stage();

        BorderPane pane = new BorderPane();

        //top
        FlowPane top = new FlowPane();
        Text header = new Text("WELCOME TO CONNECT 4!");
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        top.getChildren().add(header);
        top.setAlignment(Pos.CENTER);
        top.setPrefSize(1000, 150);
        pane.setTop(top);

        //Center
        VBox center = new VBox();

        HBox options = new HBox();
        options.setSpacing(100);

        //Options for player 2
        VBox player2box = new VBox();
        player2box.setSpacing(25);

        Text player2text = new Text("Player 2");
        player2text.setTextAlignment(TextAlignment.CENTER);
        player2box.getChildren().addAll(player2text);



        ObservableList<String> humanOrComputer = FXCollections.observableArrayList("Human", "Computer");
        final ComboBox comboBox1 = new ComboBox(humanOrComputer);
        comboBox1.setValue("Human");
        player2box.getChildren().addAll(comboBox1);

        options.getChildren().addAll(player2box);
        options.setAlignment(Pos.CENTER);

        Text ERROR_MESSAGE = new Text("Please select two different colors!\n");
        ERROR_MESSAGE.setFill(Color.RED);
        ERROR_MESSAGE.setFont(Font.font("Verdana", FontWeight.BOLD, 35));

        center.getChildren().add(options);
        center.setAlignment(Pos.CENTER);
        pane.setCenter(center);

        //bottom
        FlowPane bot = new FlowPane();

        Button enter = new Button("Ok");
        enter.setOnAction(event -> {
            if(getChoice(comboBox1.getValue())){
                    displayMain(stage);

            }else{
                gameType = 1;
                displayMain(stage);

            }



            settings.close();
        });
        enter.setPrefSize(200, 50);
        bot.getChildren().add(enter);
        bot.setAlignment(Pos.TOP_CENTER);
        bot.setPrefSize(100, 100);
        pane.setBottom(bot);
        pane.setPrefSize(800,500);
        settings.setTitle("CONNECT 4");
        settings.setScene(new Scene(pane));
        settings.show();

    }

    /** HelpSer method for settings method to see if user has select pvp or pve
     * @param choice - allows user choice to play another player or naive artificial intelligene
     */
    private boolean getChoice(Object choice){
        if(choice.equals("Human")){
            return true;
        }else{
            return false;
        }
    }

    /** Wait for the player to mark a cell
     * @throws InterruptedIOException
     */
    private void waitForPlayerAction() throws InterruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }

    /** Send this player's move to the server
     *throws IOException
     */
    private void sendMove() throws IOException {
        toServer.writeInt(columnSelected); // Send the selected column
        display(cell);
    }

    /** Receive info from the server
     * throws IOException
     */
    private void receiveInfoFromServer() throws IOException {
        // Receive game status
        int status = fromServer.readInt();

        if (status == PLAYER1_WON) {
            // Player 1 won, stop playing
            continueToPlay = false;
            if (myToken == 'X') {
                Platform.runLater(() -> lblStatus.setText("I won! (X)"));
            }
            else if (myToken == 'O') {
                Platform.runLater(() ->
                        lblStatus.setText("Player 1 (X) has won!"));
                receiveMove();
            }
        }
        else if (status == PLAYER2_WON) {
            // Player 2 won, stop playing
            continueToPlay = false;
            if (myToken == 'O') {
                Platform.runLater(() -> lblStatus.setText("I won! (O)"));
            }
            else if (myToken == 'X') {
                Platform.runLater(() ->
                        lblStatus.setText("Player 2 (O) has won!"));
                receiveMove();
            }
        }
        else if (status == DRAW) {
            // No winner, game is over
            continueToPlay = false;
            Platform.runLater(() ->
                    lblStatus.setText("Game is over, no winner!"));

            if (myToken == 'O') {
                receiveMove();
            }
        }
        else {
            receiveMove();
            Platform.runLater(() -> lblStatus.setText("My turn"));
            myTurn = true; // It is my tu                        handleConsole();rn
        }
    }

    /** gets current board shared by player1 and player2 and updates current board
     *throws I0Exception
     */

    private void receiveMove() throws IOException {
        // Get the other player's move
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        Platform.runLater(() -> cell[row][column].setToken(otherToken));
        display(cell);
    }

    /**Inner cell class is uesed to updated display for current Connect4 board
     *
     *
     */

    // An inner class for a cell
    public class Cell extends Pane {
        // Indicate the row and column of this cell in the board
        private int row;
        private int column;

        // Token used for this cell
        private char token = ' ';

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
            this.setPrefSize(2000, 2000); // What happens without this?
            setStyle("-fx-border-color: black"); // Set cell's border
            this.setOnMouseClicked(e -> handleMouseClick());

        }


        /** Return token  for current cell of seession*/
        public char getToken() {
            return token;
        }

        /** Set a new token for current cell of seession*/
        public void setToken(char c) {
            token = c;
            repaint();
        }

        /** Set a new update current board session*/

        protected void repaint() {
            if (token == 'X') {
                Line line1 = new Line(10, 10,
                        this.getWidth() - 10, this.getHeight() - 10);
                line1.endXProperty().bind(this.widthProperty().subtract(10));
                line1.endYProperty().bind(this.heightProperty().subtract(10));
                Line line2 = new Line(10, this.getHeight() - 10,
                        this.getWidth() - 10, 10);
                line2.startYProperty().bind(
                        this.heightProperty().subtract(10));
                line2.endXProperty().bind(this.widthProperty().subtract(10));

                // Add the lines to the pane
                this.getChildren().addAll(line1, line2);
            }
            else if (token == 'O') {
                Ellipse ellipse = new Ellipse(this.getWidth() / 2,
                        this.getHeight() / 2, this.getWidth() / 2 - 10,
                        this.getHeight() / 2 - 10);
                ellipse.centerXProperty().bind(
                        this.widthProperty().divide(2));
                ellipse.centerYProperty().bind(
                        this.heightProperty().divide(2));
                ellipse.radiusXProperty().bind(
                        this.widthProperty().divide(2).subtract(10));
                ellipse.radiusYProperty().bind(
                        this.heightProperty().divide(2).subtract(10));
                ellipse.setStroke(Color.BLACK);
                ellipse.setFill(Color.WHITE);

                getChildren().add(ellipse); // Add the ellipse to the pane
            }
        }

        /** Handle a mouse click event */
        private void handleMouseClick() {
            // If cell is not occupied and the player has the turn
            int col = column;

            boolean valid = false;
            while(!valid){
                if(cell[1][col].getToken() != ' '){
                    if(col < 6){
                        col++;
                    }else{
                        col--;
                    }
                }else valid =true;
            }
            if (myTurn) {
                for(int i = cell.length - 1 ; i > 1; i--){
                    if(cell[i][col].getToken() == ' '){
                        cell[i][col].setToken(myToken);
                        myTurn = false;
                        break;
                    }
                }
              //  setToken(myToken);  // Set the player's token in the cell
                columnSelected = col;
                lblStatus.setText("Waiting for the other player to move");
                waiting = false; // Just completed a successful move
            }
        }
    }

    /**Displays the current state of Connect 4 board in console
     * @param current_board
     * @return void
     */
    public void display(Cell[][] current_board){
        System.out.println(" 1 2 3 4 5 6 7");
        System.out.println("---------------");
        for (int row = 1; row < current_board.length; row++){
            System.out.print("|");
            for (int col = 1; col < current_board[1].length; col++){
                System.out.print(cell[row][col].getToken());
                System.out.print("|");
            }
            System.out.println();
            System.out.println("---------------");
        }
        System.out.println(" 1 2 3 4 5 6 7 \n");
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {

        Connect4 game = new Connect4();
        Connect4ComputerPlayer computer = new Connect4ComputerPlayer(game);
        Scanner scan = new Scanner(System.in);
        int choice;

        System.out.println("Please enter 1 to lauch gui or 2 to play in console");
        boolean valid = false;
        while(!valid) {
            try {
                choice = scan.nextInt();
                if(choice == 1){
                    launch(args);
                }else{
                    Connect4TextConsole.main(args);
                }
                valid = true;
            }
            catch(InputMismatchException e) {
                System.out.println("Please input a valid number");
                scan.nextLine();
            }
        }
    }
}