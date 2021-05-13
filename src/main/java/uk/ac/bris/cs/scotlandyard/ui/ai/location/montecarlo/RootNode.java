package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;

public interface RootNode {

    void runSingleSimulation(IntermediateScore... intermediateScores);

    void addChildren(ImmutableSet<Integer> destinations);

    @Nonnull ImmutableSet<AbstractNode> getChildren();
}
