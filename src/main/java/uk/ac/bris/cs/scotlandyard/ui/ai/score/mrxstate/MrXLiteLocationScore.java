package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.HeavyNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.DistanceMeasurer;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Reduced version of the {@link MrXLocationScore} which does not contain or use the Toml constants or any exponentials
 * in an attempt to reduce runtime. It yet unclear weather the performance difference is actually significant.
 * @see HeavyNode
 *
 */
public class MrXLiteLocationScore implements IntermediateScore {
    private final DistanceMeasurer distanceMeasurer;
    public MrXLiteLocationScore(DistanceMeasurer distanceMeasurer) {
        this.distanceMeasurer = distanceMeasurer;
    }
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        final ImmutableList<Integer> mrXLocationList = ImmutableList.of(miniBoard.getMrXLocation());
        final List<Integer> distanceToSource =
                distanceMeasurer.getDistances(mrXLocationList, miniBoard.getDetectiveLocations()).get(0);
        Collections.sort(distanceToSource);
        final int nDetectives = distanceToSource.size();
        final int closestDistance = distanceToSource.get(0)-1;
        final int averageDistance = (distanceToSource.stream().reduce(0, Integer::sum) - nDetectives)/nDetectives;
        return (double) closestDistance + averageDistance;
    }
}
