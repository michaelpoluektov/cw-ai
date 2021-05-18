package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

/**
 * To use this interface, the class must extend {@link AbstractNode}
 * Represents the root node of the tree for Monte Carlo Simulation.
 */

public interface RootNode {

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
