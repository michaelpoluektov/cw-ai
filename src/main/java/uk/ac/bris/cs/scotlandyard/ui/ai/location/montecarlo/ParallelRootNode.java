package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.mrx.HeavyRolloutMCTS_AI;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Root node for the {@link HeavyRolloutMCTS_AI} implementation.
 * @see StandardRootNode
 */

public class ParallelRootNode extends ParallelNode implements RootNode {
    private final Set<ParallelNode> children;
    private final Double explorationConstant;

    /**
     * For the root node, we call the {@link ParallelNode} constructor with a null parent.
     */
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
