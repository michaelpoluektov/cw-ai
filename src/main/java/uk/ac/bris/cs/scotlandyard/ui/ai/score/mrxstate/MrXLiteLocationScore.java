package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

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
