package fr.graynaud.multigames.view;

import javafx.scene.Parent;

public interface GameView {

    /**
     * Clean state and reset to empty, then return node
     */
    Parent activate();

    double minWidth();

    double minHeight();

    void stop();
}
