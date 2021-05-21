package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import java.util.Comparator;

/**
 * Provides a way to compare any node that implements {@link AbstractNode} using the UCT function.
 */

public class NodeUCTComparator implements Comparator<AbstractNode> {
    private final Double explorationConstant;

    /**
     * @param explorationConstant Constant using to balance exploration and exploitation. A higher value
     *                            will lead to more possibilities being explored, therefore increasing the chance of
     *                            finding a very specific sequence of moves an agent can make to increase it's score.
     *                            A lower value will increase the exploitation of moves known to be good, looking
     *                            further into the game tree and picking the best move out of a small amount of
     *                            seemingly good possibilities. Theoretical value as described in the original paper is
     *                            sqrt(2).
     */
    public NodeUCTComparator(Double explorationConstant) {
        this.explorationConstant = explorationConstant;
    }

    private Double getUCTScore(AbstractNode node){
        if(node.getParent().isEmpty()) throw new IllegalArgumentException("Root node doesn't have a parent");
        final Double exploitation = node.getAverageScore();
        final Double exploration = explorationConstant * Math.sqrt(Math.log(node.getParent().get().getPlays())/node.getPlays());
        return exploitation + exploration;
    }
    @Override
    public int compare(AbstractNode o1, AbstractNode o2) {
        return Double.compare(getUCTScore(o1), getUCTScore(o2));
    }
}