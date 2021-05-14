package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel.ParallelNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Reduced version of the {@link MrXLocationScore} which does not contain or use the toml constants in the {@link Toml} file. We
 * do this to avoid delays in {@link ParallelNode} for the
 * Monte Carlo Simulation. By not accessing the {@link Toml} file within each simulation we can increase sampling fo the decision
 * tree.
 * @see ParallelNode
 *
 */
public class MrXLiteLocationScore implements IntermediateScore {
    private final Dijkstra dijkstra;
    public MrXLiteLocationScore(Dijkstra dijkstra) {
        this.dijkstra = dijkstra;
    }
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        final ImmutableList<Integer> mrXLocationList = ImmutableList.of(miniBoard.getMrXLocation());
        final List<Integer> distanceToSource =
                dijkstra.getDistances(mrXLocationList, miniBoard.getDetectiveLocations()).get(0);
        Collections.sort(distanceToSource);
        final int nDetectives = distanceToSource.size();
        final int closestDistance = distanceToSource.get(0)-1;
        final int averageDistance = (distanceToSource.stream().reduce(0, Integer::sum) - nDetectives)/nDetectives;
        return (double) closestDistance + averageDistance;
    }

    @Nonnull
    @Override
    public Double getWeight() {
        return 1.0;
    }

}
