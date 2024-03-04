package dk.easv.bll.bot;

import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.Random;

public class AAABot implements IBot {
    Random r = new Random();

    @Override
    public IMove doMove(IGameState state) {
        IMove[] moves = state.getField().getAvailableMoves().toArray(new IMove[0]);

        double[] w = new double[moves.length]; //no of simulations that resulted in a win for i
        double[] s = new double[moves.length]; //no of simulations of i (visits)
        double c = 1.414; //exploration parameter
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
                if (s[j] < 2) { // Sim each node at least twice
                    i = j;
                    break;
                }
                if (ucb[j] > ucbMax) {
                    ucbMax = ucb[j];
                    i = j;
                }
            }

            // Simulate
            GameManager simulator = new GameManager(new GameState(state));
            simulator.updateGame(moves[i]);
            while (simulator.getGameOver() == GameManager.GameOverState.Active) {
                IMove[] availableMoves = simulator.getCurrentState().getField().getAvailableMoves().toArray(new IMove[0]);
                simulator.updateGame(availableMoves[r.nextInt(availableMoves.length)]);
            }
            s[i]++;
            t++;

            if (simulator.getGameOver() == GameManager.GameOverState.Win
                    && simulator.getCurrentPlayer() == 1) {
                w[i] += 1;
            } else if (simulator.getGameOver() == GameManager.GameOverState.Tie) {
                w[i] += 0.01;
            }

            // Backpropagate
            double underSqrt = Math.log(t) / s[i];
            double sqrt = Math.sqrt(underSqrt);
            double cSqrt = c * sqrt;
            double ws = w[i] / s[i];
            ucb[i] = ws + cSqrt;
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


