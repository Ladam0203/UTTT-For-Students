package dk.easv.bll.bot;

import com.google.gson.internal.bind.util.ISO8601Utils;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

public class AAABot implements IBot {
    private int thisPlayer;
    @Override
    public IMove doMove(IGameState state) {
        thisPlayer = state.getMoveNumber() % 2;

        IMove[] moves = state.getField().getAvailableMoves().toArray(new IMove[0]);

        RandomBot that = new RandomBot();

        GameManager[] games = new GameManager[moves.length];
        IGameState[] nextStates = new IGameState[moves.length];
        for (int i = 0; i < moves.length; i++) {
            games[i] = new GameManager(new GameState(state), that, that);
            games[i].updateGame(moves[i]);
            nextStates[i] = games[i].getCurrentState();
        }

        double[] sims = new double[moves.length];
        double[] scores = new double[moves.length];

        long startTime = System.currentTimeMillis();
        long endTime = startTime + state.getTimePerMove();
        while (System.currentTimeMillis() < endTime) {
            for (int i = 0; i < moves.length; i++) {
                //Simulate
                games[i] = new GameManager(new GameState(nextStates[i]), that, that);
                while (games[i].getGameOver() == GameManager.GameOverState.Active && games[i].updateGame()) {
                    //Wait :)
                }
                sims[i]++;

                if (games[i].getGameOver() == GameManager.GameOverState.Win) {
                    if (games[i].getCurrentPlayer() != thisPlayer) { // We won
                        scores[i] += 1;
                    }
                }
            }
        }

        //Find the best move (by highest win percentage)
        int bestMove = 0;
        for (int i = 0; i < moves.length; i++) {
            System.out.println("Move: " + i + " - " + scores[i] / sims[i] + " - " + scores[i] + " / " + sims[i]);
            if (sims[i] > 0 && scores[i] / sims[i] > scores[bestMove] / sims[bestMove]) {
                bestMove = i;
            }
        }
        //System.out.println("Best move: " + bestMove);

        return moves[bestMove];
    }

    @Override
    public String getBotName() {
        return "AAA Bot";
    }
}


