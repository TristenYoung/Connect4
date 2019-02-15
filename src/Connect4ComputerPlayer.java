/**Connect4ComputerPlaer class creates computer object to use move(int column) in Connect4 class
 *and plays against player until one wins
 *@author tristen
 *@version 1.0
 */
public class Connect4ComputerPlayer extends Connect4 {

    private int column;
    private Connect4 game;

    /**Method constructs computer object to move
     * against player
     */
    public Connect4ComputerPlayer(Connect4 game) {
        column = 1;
        this.game = game;
    }

    /**Method provides logic to the computers move and handles IllegalRowException
     * then utilizes the move(int column) method in Connect4 class
     */
    public void computerIntelligence() {

        boolean valid = false;
        if(!game.winCondition()) {
            while(!valid) {
                try {
                    game.move(this.getColumn());
                    valid = true;
                }
                catch(IllegalRowException e){
                    if (game.player == 'O'){
                        game.player = 'X';
                    }else{
                       game. player = 'O';
                    }
                    setColumn(++column);
                }
            }
        }

    }

    /**Method is a getter method for computer object
     *
     * @return int current columnn the computer is thinking of
     */
    public int getColumn() {
        return this.column;
    }

    /**Method is a setter method for computer object
     * column which will update the column the computer
     *  is thinking of after executing the logic in computerIntelligence()
     * @param column
     */
    public void setColumn(int column) {
        this.column = column;
    }

}