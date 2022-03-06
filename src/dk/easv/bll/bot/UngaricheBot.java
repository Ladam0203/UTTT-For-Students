package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;
import dk.easv.gui.UTTTGameController;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UngaricheBot implements IBot{
    Random rnd = new Random();

    List<Node> tree = new LinkedList<>();

    @Override
    public IMove doMove(IGameState state) {
        /*
        if (tree.isEmpty())
        {
            tree.add(new Node(state));
            System.out.println("Root added");
        }
        */

        //here because if we mess with the state first, then the move choosing not works... cloning?
        long time = System.currentTimeMillis();
        int i = 0;
        while (System.currentTimeMillis() - time < 999) //how does the time limit affect? if I put it over 1000ms it's still a valid move
        {
            simulateRandomGame(new GameState(state));
            i++;
        }
        System.out.println("simulated games: " + i);

        List<IMove> moves = state.getField().getAvailableMoves();
        IMove nextMove = null;
        if (moves.size() > 0) {
            nextMove = moves.get(rnd.nextInt(moves.size())); /* get random move from available moves */
        }

        return nextMove;
    }

    private boolean simulateRandomGame(IGameState state) //simulates a random game from a state, returns true if our bot wins
    {
        RandomBot randomBot = new RandomBot();
        GameManager manager = new GameManager(state);
        while (manager.getGameOver() == GameManager.GameOverState.Active)
        {
            IMove move = randomBot.doMove(manager.getCurrentState());
            boolean isValid = verifyMoveLegality(state, move);
            while(!isValid)
            {
                move = randomBot.doMove(manager.getCurrentState());
                isValid = verifyMoveLegality(state, move);
            }
            manager.updateGame(randomBot.doMove(manager.getCurrentState()));
        }
        //testing how to get if the bot or the enemy won
        //System.out.println(manager.getGameOver().toString());
        //System.out.println(manager.getCurrentPlayer() == 1);
        return manager.getGameOver() == GameManager.GameOverState.Win && manager.getCurrentPlayer() == 1;
    }

    private Boolean verifyMoveLegality(IGameState state, IMove move)
    {
        IField field = state.getField();
        boolean isValid=field.isInActiveMicroboard(move.getX(), move.getY());

        if(isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
        if(isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

        if(isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
            isValid=false;

        return isValid;
    }

    @Override
    public String getBotName() {
        return "Ungariche Bot v0.1";
    }
}

class Node{
    IGameState state;
    Node parent;
    List<Node> children = new LinkedList<>();

    float win;
    float simulation;
    float visit;

    Node(IGameState state){
        state = state;
    }
}
