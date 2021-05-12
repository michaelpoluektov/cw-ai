package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RootNode extends Node {
    private final Set<Node> children;
    protected RootNode(Board board) {
        super(new MiniBoard(board), null);
        this.children = new HashSet<>();
    }

    protected Node selectNode(){
        Node selectedNode = this;
        while(!selectedNode.isLeaf()) {
            selectedNode = Collections.max(selectedNode.getChildren());
        }
        return selectedNode;
    }

    protected void runSimulation() {
        Node selectedNode = selectNode();
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            Node selectedChild = selectedNode.getChildren().asList().get(0);
            Boolean rolloutResult = selectedChild.rollout();
            selectedChild.backPropagate(rolloutResult);
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagate(winner == MiniBoard.winner.MRX);
        }
    }

    protected void addChildren(ImmutableSet<Integer> destinations) {
        destinations.stream()
                .map(super.getMiniBoard()::advanceMrX)
                .map(advancedMiniBoard -> new Node(advancedMiniBoard, this))
                .forEach(children::add);
    }

    @Override protected ImmutableSet<Node> getChildren() {
        return ImmutableSet.copyOf(children);
    }
}
