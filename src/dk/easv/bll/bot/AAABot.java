package dk.easv.bll.bot;

import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AAABot implements IBot {
    Random r = new Random();

    @Override
    public IMove doMove(IGameState state) {
        Node root = new Node(null, state, null);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + state.getTimePerMove();
        while (System.currentTimeMillis() < endTime) {
            System.out.println("---Root node info---");
            for (Node child : root.children) {
                System.out.println(child.move + " " + child.s + " " + child.w + " " + child.getUCB());
            }
            System.out.println("--------------------");
            Node selected = select(root, 1);
            Node expanded = expand(selected);
            double result = simulate(expanded);
            backpropagate(expanded, result);
        }

        //Find the best move (by highest no of simulations)
        Node best = root.children.getFirst();
        for (Node child : root.children) {
            System.out.println(child.move + " " + child.s + " " + child.w + " " + (double) child.w / child.s);
            if (child.s > best.s) {
                best = child;
            }
        }
        System.out.println("Best move: " + best.move);

        return best.move;
    }

    private Node select(Node node, int depth) {
        if (node.children.isEmpty() || node.s < 2) {
            System.out.println("Selected node at depth " + depth + " with " + node.s + " simulations");
            return node;
        }
        Node best = node.children.getFirst();
        for (Node child : node.children) {
            if (child.getUCB() > best.getUCB()) {
                best = child;
            }
        }
        return select(best, ++depth);
    }

    private Node expand(Node node) {
        if (node.availableMoves.isEmpty()) { //No more moves to expand
            System.out.println("Tried to expand terminal node at depth " + getDepth(node));
            return node;
        }

        //Create a new node with the next available move
        GameManager simulator = new GameManager(new GameState(node.state));
        IMove move = node.popMove();
        simulator.updateGame(move);
        Node newNode = new Node(move, simulator.getCurrentState(), node);
        if (simulator.getGameOver() != GameManager.GameOverState.Active) {
            //System.out.println("Terminal node: " + move + " at depth " + getDepth(node));
            newNode.terminal = simulator.getCurrentPlayer(); //1 if win, 0 if tie/loss
        }

        node.children.add(newNode);
        System.out.println("Expanded node at depth " + getDepth(node) + " with move " + move + " and " + newNode.availableMoves.size() + " available moves");

        return newNode;
    }

    private double simulate(Node node) {
        GameManager simulator = new GameManager(new GameState(node.state));

        while (simulator.getGameOver() == GameManager.GameOverState.Active) {
            IMove[] availableMoves = simulator.getCurrentState().getField().getAvailableMoves().toArray(new IMove[0]);
            if (availableMoves.length == 0) {
                return node.terminal;
            }
            simulator.updateGame(availableMoves[r.nextInt(availableMoves.length)]);
        }
        boolean win = simulator.getGameOver() == GameManager.GameOverState.Win
                && simulator.getCurrentPlayer() == 1;

        System.out.println("We simulated a " + (win ? "win" : "loss") + " at depth " + getDepth(node));
        return win ? 1 : 0;
    }

    private void backpropagate(Node node, double result) {
        node.w += result;
        node.s++;
        if (node.parent != null) {
            backpropagate(node.parent, result);
        }
    }

    @Override
    public String getBotName() {
        return "AAA Bot";
    }

    private int getDepth(Node node) {
        int depth = 0;
        while (node.parent != null) {
            node = node.parent;
            depth++;
        }
        return depth;
    }
}

class Node {
    IMove move;
    IGameState state;
    Node parent;
    List<Node> children;
    List<IMove> availableMoves;
    double w;
    double s;
    int terminal;

    public Node(IMove move, IGameState state, Node parent) {
        this.move = move;
        this.state = state;
        this.parent = parent;
        availableMoves = new LinkedList<>(state.getField().getAvailableMoves());
        children = new LinkedList<>();
        w = 0;
        s = 0;
        terminal = -1;
    }

    public double getUCB() {
        if (s == 0) {
            return Double.MAX_VALUE;
        }
        return w / s + 1.41 * Math.sqrt(Math.log(parent.s) / s);
    }

    public IMove popMove() {
        return availableMoves.remove(0);
    }
}


