package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;

public class UngaricheBotNew implements IBot{

    Random rnd = new Random();
    int botPlayerNo = 0;

    @Override
    public IMove doMove(IGameState state) {
        botPlayerNo = state.getMoveNumber() % 2;
        Node rootNode = new Node(state, GameOverState.Active, null, null); //we are playerNo 0

        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < 10) //how does the time limit affect? if I put it over 1000ms it's still a valid move
        {
            Node promisingNode = selectPromisingNode(rootNode);
            if (promisingNode.gameOverState == GameOverState.Active)
            {
                expandNode(promisingNode);
            }


            Node nodeToExplore = promisingNode;
            if (promisingNode.children.size() > 0) {
                nodeToExplore = selectPromisingNode(nodeToExplore); //we do not randomly select...
            }

            boolean isWon = simulateRandomGame(nodeToExplore);
            backPropogation(nodeToExplore, isWon);
        }


        double maxVisit = 0;
        Node bestNode = null;
        for (Node child : rootNode.children)
        {
            if (child.visitCount > maxVisit)
            {
                maxVisit = child.visitCount;
                bestNode = child;
            }
            System.out.println(child.prevMove.getX() + " " + child.prevMove.getY() + " rate:" + child.winScore / (double)child.visitCount);
            System.out.println("w: " + child.winScore + " sim: " + child.visitCount);
        }
        System.out.println("-------------");
        return bestNode.prevMove;
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (node.children.size() != 0) {
            node = findBestNodeWithUCB(node);
        }
        return node;
    }

    private Node findBestNodeWithUCB(Node parent) {
        int parentVisit = parent.visitCount;

        double maxUcb = 0;
        Node bestNode = null;
        for (Node node : parent.children)
        {
            double ucb = ucbValue(parentVisit, node.winScore, node.visitCount);
            if (ucb > maxUcb)
            {
                maxUcb = ucb;
                bestNode = node;
            }
            //System.out.println(node.proposedMove.getX() + " " + node.proposedMove.getY() + " rate:" + node.winRate());
        }
        return bestNode;
    }

    private double ucbValue(
            int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return ((double) nodeWinScore / (double) nodeVisit)
                + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
    }

    private void expandNode(Node node)
    {
        List<IMove> moves = node.state.getField().getAvailableMoves();
        for (IMove move : moves)
        {
            node.children.add(createProgressedNode(node, move));
            if (node.winScore == Double.MIN_VALUE)
            {
                break;
            }
        }
    }

    private Node createProgressedNode(Node parent, IMove move)
    {
        GameSimulator simulator = createSimulator(parent.state);
        simulator.setGameOver(parent.gameOverState);
        simulator.updateGame(move);
        if (simulator.getGameOver() == GameOverState.Win && simulator.getCurrentPlayer() != botPlayerNo)
        {
            parent.winScore = Double.MIN_VALUE;
        }

        return new Node(simulator.getCurrentState(), simulator.getGameOver(), move, parent);
    }

    private boolean simulateRandomGame(Node node) //simulates a random game from a state, returns true if our bot wins
    {
        GameSimulator simulator = createSimulator(node.state);
        simulator.setGameOver(node.gameOverState);

        while (simulator.getGameOver() == GameOverState.Active)
        {
            List<IMove> moves = simulator.getCurrentState().getField().getAvailableMoves();
            simulator.updateGame(moves.get(rnd.nextInt(moves.size())));
        }
        //testing how to get if the bot or the enemy won
        //System.out.println(manager.getGameOver().toString());
        //System.out.println(manager.getCurrentPlayer() == 1);*/
        return simulator.getGameOver() == GameOverState.Win && simulator.getCurrentPlayer() != botPlayerNo;
    }

    private void backPropogation(Node node, boolean isWon) {
        Node tempNode = node;
        while (tempNode != null) {
            tempNode.visitCount++;
            if (isWon) {
                tempNode.winScore++;
            }
            tempNode = tempNode.parent;
        }
    }

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState(state));
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }

    @Override
    public String getBotName() {
        return "Ungariche Bot v0.2";
    }
}

class Node {
    IGameState state;
    GameOverState gameOverState;
    int visitCount = 0;
    double winScore = 0;

    IMove prevMove;
    Node parent;

    List<Node> children = new ArrayList<>();

    Node(IGameState state, GameOverState gameOverState, IMove  prevMove, Node parent){
        this.state = state;
        this.gameOverState = gameOverState;
        this.prevMove = prevMove;
        this.parent = parent;
    }
}