package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.NodeUCTComparator;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.RootNode;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ParallelRootNode extends ParallelNode implements RootNode {
    private final Set<ParallelNode> children;
    private final Double explorationConstant;
    protected ParallelRootNode(Board board, Toml constants) {
        super(new MiniBoard(board), null);
        this.children = Collections.synchronizedSet(new HashSet<>());
        this.explorationConstant = constants.getDouble("monteCarlo.explorationConstant");
    }

    protected ParallelNode selectNode(){
        Comparator<AbstractNode> uctComparator = new NodeUCTComparator(explorationConstant);
        ParallelNode selectedNode = this;
        while(!selectedNode.isLeaf()) {
            selectedNode = (ParallelNode) Collections.max(selectedNode.getChildren(), uctComparator);
        }
        return selectedNode;
    }

    @Override public void runSingleSimulation() {
        ParallelNode selectedNode = selectNode();
        selectedNode.expand();
        if(!selectedNode.getChildren().isEmpty()) {
            ParallelNode selectedChild = (ParallelNode) selectedNode.getChildren().asList().get(0);
            selectedChild.backPropagatePlays();
            Integer rolloutResult = selectedChild.rollout();
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
                .map(advancedMiniBoard -> new ParallelNode(advancedMiniBoard, this))
                .forEach(children::add);
    }

    @Nonnull
    @Override
    public ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }
}
