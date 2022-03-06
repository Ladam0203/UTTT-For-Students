package dk.easv.bll.bot;

import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.gui.UTTTGameController;

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
        if (tree.isEmpty())
        {
            tree.add(new Node(state));
            System.out.println("Root added");
        }

        simulateRandomGame(state);

        List<IMove> moves = state.getField().getAvailableMoves();
        if (moves.size() > 0) {
            return moves.get(rnd.nextInt(moves.size())); /* get random move from available moves */
        }
        return null;
    }

    private boolean simulateRandomGame(IGameState state) //simulates a random game from a state, returns true if our bot wins
    {
        GameManager gameManager = new GameManager(state, new RandomBot(), new RandomBot());

        while (gameManager.getGameOver() == GameManager.GameOverState.Active
                && gameManager.getCurrentState().getField().getAvailableMoves().size()>0) {
            //simulate a random game
        }
        return true;
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
