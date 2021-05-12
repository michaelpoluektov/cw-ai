package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class RootNode extends Node {
    private final Set<Node> children;
    private final Double explorationConstant;
    protected RootNode(Board board, Toml constants) {
        super(new MiniBoard(board), null);
        this.children = new HashSet<>();
        this.explorationConstant = constants.getDouble("monteCarlo.explorationConstant");
    }

    protected Node selectNode(){
        Comparator<Node> uctComparator = new NodeUCTComparator();
        Node selectedNode = this;
        while(!selectedNode.isLeaf()) {
            selectedNode = Collections.max(selectedNode.getChildren(), uctComparator);
        }
        return selectedNode;
    }

    protected void runSingleSimulation() {
        Node selectedNode = selectNode();
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            Node selectedChild = selectedNode.getChildren().asList().get(0);
            selectedChild.backPropagate(selectedChild.rollout(), super.getMiniBoard().getRound());
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagate(selectedNode.getMiniBoard().getRound(), super.getMiniBoard().getRound());
        }
    }

    protected void addChildren(ImmutableSet<Integer> destinations) {
        destinations.stream()
                .map(super.getMiniBoard()::advanceMrX)
                .map(advancedMiniBoard -> new Node(advancedMiniBoard, this))
                .forEach(children::add);
    }

    protected final Double getUCTScore(Node node){
        if(node.getParent().isEmpty()) throw new IllegalArgumentException("Root node doesn't have a parent");
        Double exploitation = node.getAverageScore();
        Double exploration = explorationConstant * Math.sqrt(Math.log(node.getParent().get().getPlays())/node.getPlays());
        return exploitation + exploration;
    }

    @Override protected ImmutableSet<Node> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    private class NodeUCTComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            return Double.compare(getUCTScore(o1), getUCTScore(o2));
        }
    }
}
