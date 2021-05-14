package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

/**
 * To use this interface, the class must extend {@link AbstractNode}
 * Represents the root node of the tree for Monte Carlo Simulation.
 */

public interface RootNode {

    /**
     * NOT THREAD SAFE, DO NOT USE IN A PARALLEL ENVIRONMENT
     * Any root node has the ability to run a whole simulation. A simulation consists of 4 stages:
     * 1) Select child child node of the root node with the highest UCT score
     * 2) Select node. This propagates through the tree until it find a leaf node. During propagation it always picks
     * the child with the highest UCT score.
     * 3) Rollout from the sleceted node. This expands the selected nodes children (adds them to the tree) and randomly
     * selects moves from this point until it reaches the end of the decision tree and a winner is declared.
     * 4) Backpropagation. We propagate the result/score back up the tree. Every node that was passed through, before
     * rollout, will have its number of plays incremented by 1 and score changed accordingly to the result
     * @param intermediateScores Scoring objects to be used in case of heavy rollout
     */

    void runSingleSimulation(IntermediateScore... intermediateScores);

    /**
     * Externally add children to the root node, used to initialise the tree with only reachable single destinations
     * (tickets accounted for) and the score of single simulations is low, adding double simulations
     * @param destinations MrX destinations to add
     */
    void addChildren(ImmutableSet<Integer> destinations);

    /**
     * @return Root node's children, representing MrX's available moves
     */
    @Nonnull ImmutableSet<AbstractNode> getChildren();
}
