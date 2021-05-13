package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.NodeUCTComparator;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.RootNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class StandardRootNode extends StandardNode implements RootNode {
    private final Set<StandardNode> children;
    private final Double explorationConstant;
    protected StandardRootNode(Board board, Toml constants) {
        super(new MiniBoard(board), null);
        this.children = new HashSet<>();
        this.explorationConstant = constants.getDouble("monteCarlo.explorationConstant");
    }

    protected StandardNode selectNode(){
        Comparator<AbstractNode> uctComparator = new NodeUCTComparator(explorationConstant);
        StandardNode selectedNode = this;
        while(!selectedNode.isLeaf()) {
            selectedNode = (StandardNode) Collections.max(selectedNode.getChildren(), uctComparator);
        }
        return selectedNode;
    }

    @Override public void runSingleSimulation(IntermediateScore... intermediateScores) {
        StandardNode selectedNode = selectNode();
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            StandardNode selectedChild = (StandardNode) selectedNode.getChildren().asList().get(0);
            selectedChild.backPropagatePlays();
            Integer rolloutResult = selectedChild.rollout(intermediateScores);
            selectedChild.backPropagateScore(rolloutResult, super.getRound());
        } else {
            MiniBoard.winner winner = selectedNode.getMiniBoard().getWinner();
            if(winner == MiniBoard.winner.NONE) throw new RuntimeException("State with no children has no winner!");
            selectedNode.backPropagatePlays();
            selectedNode.backPropagateScore(selectedNode.getRound(), super.getRound());
        }
    }

    @Override public void addChildren(ImmutableSet<Integer> destinations) {
        destinations.stream()
                .map(super.getMiniBoard()::advanceMrX)
                .map(advancedMiniBoard -> new StandardNode(advancedMiniBoard, this))
                .forEach(children::add);
    }

    @Override @Nonnull public ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }
}
