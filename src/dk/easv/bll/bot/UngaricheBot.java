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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UngaricheBot implements IBot{
    Random rnd = new Random();

    @Override
    public IMove doMove(IGameState state) {
        //SET UP POSSIBLE NODES---
        List<IMove> moves = state.getField().getAvailableMoves();
        List<ExperimentNode> nodes = new ArrayList<>();
        for (IMove move : moves)
        {
            nodes.add(new ExperimentNode(state, move));
        }

        //EXPLORE---
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < 1000) //how does the time limit affect? if I put it over 1000ms it's still a valid move
        {
            for (ExperimentNode node : nodes)
            {
                if (simulateRandomGame(node))
                {
                    node.win++;
                }
                node.sim++;
            }
        }

        //EXPLOIT---
        float maxWinRate = 0;
        ExperimentNode bestNode = null;
        for (ExperimentNode node : nodes)
        {
            if (node.winRate() > maxWinRate)
            {
                maxWinRate = node.winRate();
                bestNode = node;
            }
            //System.out.println(node.proposedMove.getX() + " " + node.proposedMove.getX() + " rate:" + node.winRate());
        }

        if (bestNode != null)
        {
            return bestNode.proposedMove;
        }
        //just in case, if there is an error: (sometimes best move is null?)
        return moves.get(rnd.nextInt(0, moves.size()));
    }

    private boolean simulateRandomGame(ExperimentNode experimentNode) //simulates a random game from a state, returns true if our bot wins
    {
        RandomBot randomBot = new RandomBot();
        GameManager manager = new GameManager(new GameState(experimentNode.state));

        manager.updateGame(experimentNode.proposedMove);

        while (manager.getGameOver() == GameManager.GameOverState.Active)
        {
            IMove move = randomBot.doMove(manager.getCurrentState());
            boolean isValid = verifyMoveLegality(manager.getCurrentState(), move);
            while(!isValid)
            {
                move = randomBot.doMove(manager.getCurrentState());
                isValid = verifyMoveLegality(manager.getCurrentState(), move);
            }
            manager.updateGame(randomBot.doMove(manager.getCurrentState()));
        }
        //testing how to get if the bot or the enemy won
        //System.out.println(manager.getGameOver().toString());
        //System.out.println(manager.getCurrentPlayer() == 1);*/
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

class ExperimentNode{
    IGameState state;
    IMove proposedMove;

    float win = 0;
    float sim = 0;

    ExperimentNode(IGameState state, IMove proposedMove){
        this.state = state;
        this.proposedMove = proposedMove;
    }

    float winRate()
    {
        return win/sim;
    }
}
