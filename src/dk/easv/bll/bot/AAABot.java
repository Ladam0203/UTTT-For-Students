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

        RandomBot rnd1 = new RandomBot();
        RandomBot rnd2 = new RandomBot();

        GameManager[] games = new GameManager[moves.length];
        IGameState[] nextStates = new IGameState[moves.length];
        for (int i = 0; i < moves.length; i++) {
            games[i] = new GameManager(new GameState(state));
            games[i].updateGame(moves[i]);
            nextStates[i] = games[i].getCurrentState();
        }

        double[] w = new double[moves.length]; //no of simulations that resulted in a win for i
        double[] s = new double[moves.length]; //no of simulations of i (visits)
        double c = Math.sqrt(2); //exploration parameter
        double t = 0; //total number of simulations
        double[] ucb = new double[moves.length]; //ucb value of i
        for (int i = 0; i < moves.length; i++) {
            ucb[i] = Double.MAX_VALUE;
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + state.getTimePerMove() - 900;
        while (System.currentTimeMillis() < endTime) {
            // Select
            int i = 0;
            double ucbMax = -1;
            for (int j = 0; j < moves.length; j++) {
                if (s[j] < 2) {
                    i = j;
                    break;
                }
                if (ucb[j] > ucbMax) {
                    ucbMax = ucb[j];
                    i = j;
                }
            }

            // Simulate
            games[i] = new GameManager(new GameState(nextStates[i]), rnd1, rnd2);
            while (games[i].getGameOver() == GameManager.GameOverState.Active) {
                games[i].updateGame();
            }
            s[i]++;
            t++;

            if (games[i].getGameOver() == GameManager.GameOverState.Win) {
                if (games[i].getCurrentPlayer() != thisPlayer) { // We won
                    w[i] += 1;
                }
            }

            // Backpropagate
            ucb[i] = (w[i] / s[i]) + (c * Math.sqrt(Math.log(t) / s[i]));
        }

        //Find the best move (by highest no of simulations)
        int bestMove = 0;
        double sMax = -1;
        for (int i = 0; i < moves.length; i++) {
            System.out.printf("Move %d: %f/%f + %f * √(lg(%f)/%f) = %f\n", i, w[i], s[i], c, Math.log(t), s[i], ucb[i]);
            if (s[i] > sMax) {
                sMax = s[i];
                bestMove = i;
            }
        }

        return moves[bestMove];
    }

    @Override
    public String getBotName() {
        return "AAA Bot";
    }
}


