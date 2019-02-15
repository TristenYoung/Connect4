/**Connect4 class that creates mechanics for viable 6x7 Connect 4 game
 * turn, player, and current_board are public methods that can be illustrated for current game status
 * @author Tristen Young
 * @version 1.0
 *
 */
import java.awt.Choice;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Observable;

public class Connect4 extends Observable {

    public static int turn = 1;
    public char player = 'O';
    public static int current_row = 1;
    public char[][] current_board = createBoard();

    public static char[][] createBoard(){

        char[][] new_board = new char[7][8];

        for (int row = 1; row < new_board.length; row++){
            for (int col = 1; col < new_board[0].length; col++){
                new_board[row][col] = ' ';
            }
        }

        return new_board;
    }

    /**Method will return boolean to inform whether or not a player
     *  will move against another player or computer
     * @return boolean
     */
    public static boolean playMode() {

        Scanner choose = new Scanner(System.in);
        int choice = 1;

        boolean valid = false;
        while(!valid) {
            try {
                choice = choose.nextInt();
                valid = true;
            }
            catch(InputMismatchException e) {
                System.out.println("Please input a valid number");
                choose.nextLine();
            }
        }


        if(choice == 1) {

            return true;
        }else{

            return false;
        }
    }

    /**Method will return boolean to inform whether or not a player
     *  will move against another player or computer
     * @return boolean
     */
    public boolean playGUI() {

        Scanner choose = new Scanner(System.in);
        int choice = 1;

        boolean valid = false;
        while(!valid) {
            try {
                choice = choose.nextInt();
                valid = true;
            }
            catch(InputMismatchException e) {
                System.out.println("Please input a valid number");
                choose.nextLine();
            }
        }


        if(choice == 1) {

            return true;
        }else{

            return false;
        }
    }


    /**Method allows for next available player to drop there token in a valid column
     * @exception InputMismatchException happens when user puts a anything besides int and will cause system exit
     * @return void
     */
    public void move() {

        if (player == 'O'){
            player = 'X';
        }else{
            player = 'O';
        }

        Scanner scan = new Scanner(System.in);

        int column = 1;


        System.out.print("Player " + player + ", Choose a column number from 1-7: ");


        boolean valid = false;
        while(!valid) {
            try {
                column = scan.nextInt();
                valid = true;

            }
            catch(InputMismatchException e) {
                System.out.println("please enter a valid column number");
                scan.nextLine();
            }
        }



        while(!validate(column,current_board)) {
            column = scan.nextInt();
        }

        for (int row = current_board.length-1; row >= 1; row--){
            if(current_board[row][column] == ' '){
                current_board[row][column] = player;
                break;
            }
        }

        turn++;
    }

    /**Method allows for next available player to drop
     * their token in a valid column
     * @param column
     * @throws IllegalRowException
     */
    public void move(int column) throws IllegalRowException{

        if (player == 'O'){
            player = 'X';
        }else{
            player = 'O';
        }

        if(validate(column,current_board)) {
            for (int row = current_board.length-1; row >= 1; row--){
                if(current_board[row][column] == ' '){
                    current_board[row][column] = player;
                    current_row = row;

                    break;
                }
            }
            turn++;
        }else {
            throw new IllegalRowException();
        }
        setChanged();
        notifyObservers();
    }



    /**Verifies that column entered is within the game board
     * method also verifies that row is not full
     * if either of these are not valid then validate will be false
     *
     * @param column
     * @param current_board
     * @return boolean
     */
    public boolean validate(int column, char[][] current_board){

        if (column < 1 || column > current_board[1].length || column > 7){
            System.out.println("Column does not exist");
            return false;
        }

        if (current_board[1][column] != ' '){
            System.out.println("Row is full");
            return false;
        }

        return true;
    }

    /**Method will cue the end of the game
     *
     * @return boolean
     */
    public boolean winCondition() {
        if(turn >= 42) {
            return true;
        }

        boolean win = isWinner(player,current_board);

        return win;
    }

    /**Method checks if any player have won the game
     * by checking the array for 4 X/0 in horizontal, vertical
     * or diagonal being nextto each other in the 2d array
     *
     * @param player
     * @param current_board
     * @return boolean
     */
    public boolean isWinner(char player, char[][] current_board){

        for(int row = 1; row<current_board.length; row++){
            for (int col = 1;col < current_board[1].length - 3; col++){
                if (current_board[row][col] == player   &&
                        current_board[row][col+1] == player &&
                        current_board[row][col+2] == player &&
                        current_board[row][col+3] == player){
                    System.out.println("Horizontal");
                    return true;
                }
            }
        }

        for(int row = 1; row < current_board.length - 3; row++){
            for(int col = 1; col < current_board[1].length; col++){
                if (current_board[row][col] == player   &&
                        current_board[row+1][col] == player &&
                        current_board[row+2][col] == player &&
                        current_board[row+3][col] == player){
                    System.out.println("Vertical");
                    return true;
                }
            }
        }

        for(int row = 3; row < current_board.length; row++){
            for(int col = 1; col < current_board[1].length - 3; col++){
                if (current_board[row][col] == player   &&
                        current_board[row-1][col+1] == player &&
                        current_board[row-2][col+2] == player &&
                        current_board[row-3][col+3] == player){
                    System.out.println("Diagonal");
                    return true;
                }
            }
        }

        for(int row = 1; row < current_board.length - 3; row++){
            for(int col = 1; col < current_board[1].length - 3; col++){
                if (current_board[row][col] == player   &&
                        current_board[row+1][col+1] == player &&
                        current_board[row+2][col+2] == player &&
                        current_board[row+3][col+3] == player){
                    System.out.println("Diagonal");
                    return true;
                }
            }
        }
        return false;
    }
}