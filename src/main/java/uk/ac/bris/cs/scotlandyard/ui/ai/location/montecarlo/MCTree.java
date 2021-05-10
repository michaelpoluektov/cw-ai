package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;

import java.util.ArrayList;
import java.util.Collections;

public class MCTree {
    private final MCNode rootNode;
    private final Board board;

    protected MCTree(Board board, Toml constants){
        rootNode = new MCNode(board, constants);
        this.board = board;
    }

    protected MCNode selectNode(MCNode node){ // what
        MCNode selectedNode = rootNode;
        while(!selectedNode.isLeaf()) {
            selectedNode = Collections.max(selectedNode.getChildren());
        }
        return selectedNode;
    }

    protected MCNode getRootNode() {
        return rootNode;
    }

    protected void expandDoubleChildren(){
        ArrayList<MCNode> newChildren = new ArrayList<>(rootNode.getChildren());
        ImmutableSet<MCNode> doubleChildren;
        doubleChildren = board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.DoubleMove)
                .map(move -> ((Move.DoubleMove) move).destination2)
                .map(rootNode.getMiniBoard()::advanceMrX)
                .map(miniBoard1 -> new MCNode(miniBoard1, rootNode))
                .collect(ImmutableSet.toImmutableSet());
        newChildren.addAll(doubleChildren);
        rootNode.setChildren(ImmutableSet.copyOf(newChildren));
    }

    protected void runSimulation() {
        MCNode selectedNode = selectNode(rootNode);
        selectedNode.expandSingleChildren();
        if (!selectedNode.getChildren().isEmpty()) {
            MCNode selectedChild = selectedNode.getChildren().asList().get(0);
            Boolean rolloutResult = selectedChild.rollout();
            selectedChild.backPropagate(rolloutResult);
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if (winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagate(winner == MiniBoard.winner.MRX);
        }
    }
}
