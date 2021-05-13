package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

public interface RootNode {

    void runSingleSimulation();

    void addChildren(ImmutableSet<Integer> destinations);

    @Nonnull ImmutableSet<AbstractNode> getChildren();
}
