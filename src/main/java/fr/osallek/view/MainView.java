package fr.osallek.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class MainView {

    public static final int PADDING = 24;

    private static final ResourceBundle I18N = ResourceBundle.getBundle("i18n.main");

    private final BorderPane root;

    private final Stage stage;

    private SudokuView sudokuView;

    public MainView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.root.setBackground(Background.fill(Color.WHITE));
        MenuBar menuBar = new MenuBar();

        Menu gamesMenu = new Menu(I18N.getString("games"));
        MenuItem sudokuMenuItem = new MenuItem(I18N.getString("sudoku"));
        sudokuMenuItem.setOnAction(event -> {
            if (this.sudokuView == null) {
                this.sudokuView = new SudokuView();
            }

            changeActivePane(this.sudokuView);
        });

        gamesMenu.getItems().add(sudokuMenuItem);
        menuBar.getMenus().add(gamesMenu);
        this.root.setTop(menuBar);

        //For now
        if (this.sudokuView == null) {
            this.sudokuView = new SudokuView();
        }
        changeActivePane(this.sudokuView);
    }

    private void changeActivePane(GameView view) {
        this.root.setCenter(view.activate());
        this.root.minHeight(view.minHeight() + 2d * PADDING);
        this.root.minWidth(view.minWidth() + 2d * PADDING);
        BorderPane.setAlignment(this.root, Pos.CENTER);
        this.stage.setMinWidth(view.minWidth() + 2d * PADDING);
        this.stage.setMinHeight(view.minHeight() + 2d * PADDING);
    }

    public BorderPane getRoot() {
        return root;
    }

    public void stop() {}
}
