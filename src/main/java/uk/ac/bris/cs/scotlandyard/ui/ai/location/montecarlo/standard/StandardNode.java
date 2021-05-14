package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.standard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.AbstractNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel.ParallelNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  We populate our standard MonteCarlo tree using StandardNodes
 */
public class StandardNode extends AbstractNode {
    private int plays;
    private int score;
    private final Set<StandardNode> children;
    protected StandardNode(MiniBoard miniBoard,
                           StandardNode parent) {
        super(miniBoard, parent);
        this.plays = 0;
        this.score = 0;
        this.children = new HashSet<>();
    }
    @Override
    public Double getAverageScore() {
        return (double) score/plays;
    }

    @Override
    public Integer getPlays() {
        return plays;
    }

    @Override
    public ImmutableSet<AbstractNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    /**
     * Propagates the plays back up the tree so long as the current node has a parent. We separated this from backPropagateScore
     * in order to implement {@link ParallelNode} which requires this.
     * @see ParallelNode
     */

    public void backPropagatePlays() {
        plays++;
        getParent().ifPresent(AbstractNode::backPropagatePlays);
    }

    /**
     * The score backpropagated to MrX's child node is the number of rounds MrX losses in from the round at which he starts
     * simulation. We update the child's score instead of its parent due to the fact that each child nodes statistics are used
     * for its parents decision. Similar idea's are used for the detectives.
     * @param round Round at which the game has ended
     * @param rootNodeRound Round at which the simulations started from
     */
    @Override
    public void backPropagateScore(Integer round, Integer rootNodeRound) {
        if(getParent().isPresent()) {
            if(getParent().get().getMiniBoard().getMrXToMove()) score += (round - rootNodeRound);
            else score += getRoundSize() + 1 - round;
            getParent().get().backPropagateScore(round, rootNodeRound);
        }
    }

    /**
     * Standard implementation of the rollout method. This randomly slects a path to the bottom of the decision tree
     * @param intermediateScore Ignored in the standard version of MCTS. This is considered a "light" playout.
     * @see ParallelNode for an implementation of a "heavy" playout
     * @return
     */
    @Override
    public Integer rollout(IntermediateScore... intermediateScore) {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not rollout from tree node!");
        MiniBoard rollingMiniBoard = getMiniBoard();
        while(rollingMiniBoard.getWinner() == MiniBoard.winner.NONE) {
            ImmutableList<MiniBoard> availableMiniBoards = rollingMiniBoard.getAdvancedMiniBoards().asList();
            rollingMiniBoard = availableMiniBoards.get(new Random().nextInt(availableMiniBoards.size()));
        }
        if(rollingMiniBoard.getWinner() == MiniBoard.winner.MRX) return getRoundSize()+1;
        else return rollingMiniBoard.getRound();
    }

    /**
     * For expansion of a leaf node. This adds child nodes to the caller node. These child nodes are standard nodes and
     * each of which contains an advanced {@link MiniBoard}
     */
    @Override
    public void expand() {
        if(!isLeaf()) throw new UnsupportedOperationException("Can not populate tree node!");
        if(getMiniBoard().getWinner() == MiniBoard.winner.NONE) {
            children.addAll(getMiniBoard().getAdvancedMiniBoards().stream()
                    .map(value -> new StandardNode(value, this))
                    .collect(Collectors.toSet()));
        }
    }
}
