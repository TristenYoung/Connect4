/**Connect4TextConsole class displays mechanics for viable 6x7 Connect 4 game mecahnics and current state of board for player knowledge
 * turn, player, and current_board are public methods that can be illustrated for current game status
 * @author Tristen Young
 * @version 2.0
 *
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Connect4TextConsole extends Connect4 implements Connect4Constants {

    // Indicate whether the player has the turn
    private boolean myTurn = false;

    private boolean playgGui = true;
    private int gameType = 0;

    // Indicate the token for the player
    private char myToken = ' ';

    // Indicate the token for the other player
    private char otherToken = ' ';

    // Create and initialize cells
    private char[][] cell =  new char[7][8];

    // Indicate selected row and column by the current move
    private int columnSelected;

    // Host name or ip
    private String host = "localhost";

    // Input and output streams from/to server
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // Continue to play?
    private boolean continueToPlay = true;

    // Wait for the player to mark a cell
    private boolean waiting = true;


    /**displayMain creates game board and connects board to server to operate on it logic
     *
     */
    public void displayMain(){


        for (int i = 1; i < 7; i++)
            for (int j = 1; j < 8; j++)
                cell[i][j] = ' ';

        display(cell);

        // Connect to the server
        connectToServer();
    }

    /**ConnectToServer  communicates with Connect4Server to check with connect$ game logic
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
                        System.out.println("Player 1 with token 'X'");
                        System.out.println("Waiting for player 2 to join");


                    // Receive startup notification from the server
                    fromServer.readInt(); // Whatever read is ignored

                    // The other player has joined
                            System.out.println("Player 2 has joined. I start first");

                    // It is my turn
                    myTurn = true;
                }
                else if (player == PLAYER2) {
                    myToken = 'O';
                    otherToken = 'X';
                        System.out.println("Player 2 with token 'O'");
                        System.out.println("Waiting for player 1 to move");

                }

                // Continue to play
                while (continueToPlay) {
                    if (player == PLAYER1) {
                        handleConsole();
                        waitForPlayerAction(); // Wait for player 1 to move
                        sendMove(); // Send the move to the server
                        receiveInfoFromServer(); // Receive info from the server
                    }
                    else if (player == PLAYER2) {
                        receiveInfoFromServer(); // Receive info from the server
                        if(continueToPlay){
                            handleConsole();
                        }
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

    /**handleConsole allows for input from user to select a valid column to move
     *
     */
    private void handleConsole(){
        Scanner scan = new Scanner(System.in);
        int col = 1;
        boolean valid = false;

        while(!valid){
            try {
                System.out.println("Please choose a column by typing in 1-7");
                col = scan.nextInt();
                if(col > 7){
                 scan.nextLine();
                }else{
                    valid = true;
                }
            }
            catch (InputMismatchException e){
                scan.nextLine();
            }
        }

        valid = false;
        while(!valid){
            if(cell[1][col] != ' '){
                if(col < 6){
                    col++;
                }else{
                    col--;
                }
            }else valid =true;
        }

        if (myTurn) {
            for(int i = cell.length - 1 ; i > 1; i--){
                if(cell[i][col] == ' '){
                    cell[i][col] = myToken;
                    myTurn = false;
                    break;
                }
            }
            //  setToken(myToken);  // Set the player's token in the cell
            columnSelected = col;
            System.out.println("Waiting for the other player to move");
            waiting = false; // Just completed a successful move
        }
    }

    /**Displays the setting options for Connect 4 game gui
     */
    public void settings(){

            if(playgGui){
                displayMain();

            }else{
                gameType = 1;
                displayMain();

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
                System.out.println("I won! (X)");
            }
            else if (myToken == 'O') {

                        System.out.println("Player 1 (X) has won!");
                receiveMove();
            }
        }
        else if (status == PLAYER2_WON) {
            // Player 2 won, stop playing
            continueToPlay = false;
            if (myToken == 'O') {
                System.out.println("I won! (O)");
            }
            else if (myToken == 'X') {

                        System.out.println("Player 2 (O) has won!");
                receiveMove();
            }
        }
        else if (status == DRAW) {
            // No winner, game is over
            continueToPlay = false;

                    System.out.println("Game is over, no winner!");

            if (myToken == 'O') {
                receiveMove();
            }
        }
        else {
            receiveMove();
            System.out.println("My turn");
            myTurn = true; // It is my turn
        }
    }

    /** gets current board shared by player1 and player2 and updates current board
     *throws I0Exception
     */

    private void receiveMove() throws IOException {
        // Get the other player's move
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        cell[row][column] = otherToken;
        display(cell);
    }

    /**Displays the current state of Connect 4 board
     *
     * @param current_board
     * @return void
     */
    public void display(char[][] current_board){
        System.out.println(" 1 2 3 4 5 6 7");
        System.out.println("---------------");
        for (int row = 1; row < current_board.length; row++){
            System.out.print("|");
            for (int col = 1; col < current_board[1].length; col++){
                System.out.print(cell[row][col]);
                System.out.print("|");
            }
            System.out.println();
            System.out.println("---------------");
        }
        System.out.println(" 1 2 3 4 5 6 7 \n");
    }


    /**Method allows for the running of a new Connect 4 game
     *
     * @param args
     */
    public static void main(String[] args) {

        Connect4 game = new Connect4();
        Connect4ComputerPlayer computer = new Connect4ComputerPlayer(game);
        Connect4TextConsole console = new Connect4TextConsole();
        Scanner scan = new Scanner(System.in);
        int choice;

        boolean valid = false;
        while(!valid) {
            try {
                System.out.println("Please enter 1 to player computer or 2 to play human");
                choice = scan.nextInt();
                if(choice == 1) {

                    console.gameType = 1;
                    console.displayMain();
                    }else{
                    console.displayMain();
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