/**IllegalRowException class extends Exception for when the board
 *  in Connect4 no longer has available rows
 * @author tristen
 * @version 1.0
 */
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Connect4GUI extends Application{
    private final static int COLUMNS = 7;
    private final static int ROWS = 6;
    private final static int TILE_SIZE = 80;
    private boolean redMove = true;
    private Disk[][] grid = new Disk[8][8];
    private Connect4 game = new Connect4();
    private Pane diskRoot = new Pane();

    private static int player1 = 'X';
    private static int player2 = 'O';
    private static int winner = 3;


    private Stage stage;
    private BorderPane pane;
    private Shape gridShape;
    private Text status;

    public static boolean full;
    public static boolean colFull;

    /**init method creates a new Connect4 game board
     *
     */
    public void init(){

        this.game = new Connect4();
        full = false;
        colFull = false;
    }

    /**Starts the GUI
     *
     * @param stage
     */
    public void start(Stage stage){
        this.stage = stage;
        settings();
    }

    /**Creates the buttons for use in pvp
     * @return buttons - for user to select which column to drop game peice
     */
    private HBox makeButtonPane() {
        HBox buttons = new HBox();

        for (int i = 1; i <= COLUMNS; i++) {
            int col = i;
            Button button = new Button("" + col + "");
            button.setPrefWidth(100);
            button.setOnAction(event -> placeDisk(new Disk(redMove), col));
            buttons.getChildren().add(button);
        }

        return buttons;
    }

    /**Creates the buttons for use in pve
     * @return buttons - for user to select which column to drop game peice
     * and the computer player will also place disk on input
     */
    private HBox makeComputerButtonPane1() {
        HBox buttons = new HBox();

        for (int i = 1; i <= COLUMNS; i++) {
            int col = i;
            Button button = new Button("" + col + "");
            button.setPrefWidth(100);
            button.setOnAction(event -> {placeDisk(new Disk(redMove), col); computerPlaceDisk(new Disk(redMove));});
            buttons.getChildren().add(button);
        }

        return buttons;
    }

    /**Creates the grid of with cut out of circles
     * @return shape - gameboard rectangle with circles cut out for peices to be placed
     */
    public Shape makeGrid(){

        Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);

        for(int y = 0; y < ROWS; y++){
            for(int x = 0; x < COLUMNS; x++){
                Circle circle =  new Circle(TILE_SIZE / 4);
                circle.setCenterX(TILE_SIZE / 3);
                circle.setCenterY(TILE_SIZE / 3);
                circle.setTranslateX(x * (TILE_SIZE) + TILE_SIZE / 2);
                circle.setTranslateY(y * (TILE_SIZE) + TILE_SIZE / 2);

                shape = Shape.subtract(shape, circle);
            }
        }
        return shape;
    }

    /**Method places game peice in selected column of player
     */
    private void placeDisk(Disk disc, int column){

        int col = column;
        boolean valid = false;
        while(!valid){
            try{
                game.move(col);
                valid = true;
            }
            catch (IllegalRowException e){
                if(col > 6){
                    if (game.player == 'O'){
                        game.player = 'X';
                    }else{
                        game.player = 'O';
                    }
                    col--;
                }
                if(col < 6){
                    if (game.player == 'O'){
                        game.player = 'X';
                    }else{
                        game.player = 'O';
                    }
                    col++;
                }

            }
        }

                grid[game.current_row][col] = disc;
                diskRoot.getChildren().add(disc);
                disc.setTranslateX(col * (TILE_SIZE - 8) + TILE_SIZE / 8);
                disc.setTranslateY(game.current_row * (TILE_SIZE - 5) + TILE_SIZE / 12);
                if(game.winCondition()){
                  disableButtons();
                    if(game.turn == 41){
                        popup(false);
                    }else{
                        popup(true);
                    }
                 }
                redMove = !redMove;

    }

    /**Method allows computer to place game peice in calculated column
     */
    private void computerPlaceDisk(Disk disc){

        Connect4ComputerPlayer computerPlayer = new Connect4ComputerPlayer(game);
        computerPlayer.computerIntelligence();
        int col = computerPlayer.getColumn();



        grid[game.current_row][col] = disc;
        diskRoot.getChildren().add(disc);
        disc.setTranslateX(col * (TILE_SIZE - 8) + TILE_SIZE / 8);
        disc.setTranslateY(game.current_row * (TILE_SIZE - 5) + TILE_SIZE / 12);
        if(game.winCondition()){
            disableButtons();
            if(game.turn == 41){
                popup(false);
            }else{
                popup(true);
            }
        }

        redMove = !redMove;
    }

    /**Inner Class Disk creates disk objects to be used as game peices
     *
     */

    private static class Disk extends Circle{

        private final boolean red;
        public Disk(boolean red){
            super(TILE_SIZE / 2, red ? Color.RED : Color.YELLOW);
            this.red = red;

            setCenterX(TILE_SIZE / 2);
            setCenterY(TILE_SIZE / 2);
        }
    }

    /**Creates the end of game popup
     * @param bool - true if player has won, false if it is a tie
     */
    public void popup(boolean bool){
        Stage popup = new Stage();
        FlowPane pane = new FlowPane();
        VBox box = new VBox();
        HBox buttons = new HBox();

        //Sets the text for the end of game popup
        Text result;
        if (bool) {
            result = new Text("GAME OVER! PLAYER " + game.player + " WINS!");
            result.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        } else {
            result = new Text("GAME OVER! TIE GAME!");
            result.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        }


        Button ok = new Button("OK");
        ok.setOnAction(event -> {System.exit(0);});
        ok.setPrefWidth(100);
        ok.setLineSpacing(100);
        buttons.setSpacing(50);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(ok);

        //Centers the button
        box.setSpacing(35);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(result, buttons);

        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(box);

        //Sets size of popup
        pane.setPrefSize(730, 250);

        popup.setTitle("GAME OVER");
        popup.setScene(new Scene(pane));
        popup.show();
    }



    /**Enables the buttons
     * Allows them to be clickable
     */
    public void enableButtons(){
        FlowPane pane = ((FlowPane)((BorderPane) this.pane.getCenter()).getTop());
        HBox box = (HBox)pane.getChildren().get(1);
        for (Node button: box.getChildren()){
            Button k = (Button) button;
            k.setDisable(false);
        }
    }

    /**Disables the buttons
     * Makes the buttons unclickable
     */
    public void disableButtons(){
        FlowPane pane = ((FlowPane)((BorderPane) this.pane.getCenter()).getTop());
        HBox box = (HBox)pane.getChildren().get(0);
        for (Node button: box.getChildren()){
            Button k = (Button) button;
            k.setDisable(true);
        }
    }

    /**Displays the setting options for Connect 4 game
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
                try {
                    displayMain();
                } catch (IllegalRowException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    displayComputerMain();
                } catch (IllegalRowException e) {
                    e.printStackTrace();
                }
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

    /** Hekper method for settings method to see if user has select pvp or pve
     */
    private boolean getChoice(Object choice){
        if(choice.equals("Human")){
            return true;
        }else{
            return false;
        }
    }

    /**Displays the game board for pvp
     * @exception IllegalRowException
     */
    public void displayMain() throws IllegalRowException {
        BorderPane pane = new BorderPane();

        //top
        FlowPane topPane = new FlowPane();
        this.status = new Text("Player " + game.player + "'s turn!");
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(status);
        pane.setTop(topPane);

        //center
        BorderPane innerPane = new BorderPane();

        innerPane.getChildren().add(diskRoot);

        FlowPane top = new FlowPane();
        HBox buttons = makeButtonPane();
        top.setAlignment(Pos.CENTER);
        top.getChildren().add(buttons);
        innerPane.setTop(top);

        FlowPane center = new FlowPane();

        gridShape = makeGrid();
        center.setAlignment(Pos.CENTER);
        center.getChildren().add(gridShape);
        innerPane.setCenter(center);

        pane.setCenter(innerPane);

        //bottom
        FlowPane bot = new FlowPane();
        bot.setAlignment(Pos.CENTER);
        pane.setBottom(bot);

        this.pane = pane;

        stage.setTitle("Connect 4");
        stage.setScene(new Scene(pane));
        stage.show();
    }

    /**Displays the game board for pve
     * @exception IllegalRowException
     */
    public void displayComputerMain() throws IllegalRowException {
        BorderPane pane = new BorderPane();

        //top
        FlowPane topPane = new FlowPane();
        this.status = new Text("Player " + game.player + "'s turn!");
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(status);
        pane.setTop(topPane);

        //center
        BorderPane innerPane = new BorderPane();

        innerPane.getChildren().add(diskRoot);

        FlowPane top = new FlowPane();
        HBox buttons = makeComputerButtonPane1();
        top.setAlignment(Pos.CENTER);
        top.getChildren().add(buttons);
        innerPane.setTop(top);

        FlowPane center = new FlowPane();

        gridShape = makeGrid();
        center.setAlignment(Pos.CENTER);
        center.getChildren().add(gridShape);
        innerPane.setCenter(center);

        pane.setCenter(innerPane);

        //bottom
        FlowPane bot = new FlowPane();

        bot.setAlignment(Pos.CENTER);
        pane.setBottom(bot);

        this.pane = pane;

        stage.setTitle("Connect 4");
        stage.setScene(new Scene(pane));
        stage.show();
    }

    /**Method allows for the running of a new Connect 4 Gui Program
     *
     * @param args
     */
    public static void main(String[] args){
        Application.launch(args);
    }
}