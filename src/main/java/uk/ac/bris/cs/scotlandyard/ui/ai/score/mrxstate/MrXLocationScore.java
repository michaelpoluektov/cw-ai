package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.DistanceMeasurer;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

/**
 * Scores a location based on how far MrX is away from the detectives.
 */

public class MrXLocationScore implements IntermediateScore {
    private final Double locationScoreExp;
    private final Double locationScoreWeight;
    private final DistanceMeasurer distanceMeasurer;

    /**
     * @param distanceMeasurer Pass an instantiation of {@link Dijkstra} to get the shortest distance to the detectives
     */
    public MrXLocationScore(Toml constants, DistanceMeasurer distanceMeasurer) {
        this.locationScoreExp = constants.getDouble("location.exp");
        this.locationScoreWeight = constants.getDouble("location.weight");
        this.distanceMeasurer = distanceMeasurer;
    }

    /**
     * Returns the score based on distances to detectives. We use a series of weights based on the sum to infinity of the
     * radii from detectives to MrX. We weight each score accordingly depending on how close each detective is. The
     * further a detective is away the less weight it should bare on the overall location score.
     * @param miniBoard MiniBoard to score
     */
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        final ImmutableList<Integer> mrXLocationList = ImmutableList.of(miniBoard.getMrXLocation());
        final List<Integer> distanceToSource =
                distanceMeasurer.getDistances(mrXLocationList, miniBoard.getDetectiveLocations()).get(0);
        Collections.sort(distanceToSource);
        List<Double> weightsList = distanceToSource.stream()
                .map(value -> pow(locationScoreExp, -value)/(1 - pow(locationScoreExp, -1)))
                .collect(Collectors.toList());
        final Double weightsListSum = weightsList.stream().reduce(0.0,Double::sum);
        weightsList = weightsList.stream().map(value -> value/weightsListSum).collect(Collectors.toList());


        double totalScore = 0.0;
        for(int i = 0; i < distanceToSource.size(); i++) {
            final Double unweightedScore = 1 - pow(locationScoreExp, 1 - distanceToSource.get(i));
            totalScore += weightsList.get(i)*unweightedScore;
        }
        return totalScore;
    }

    @Nonnull
    @Override
    public Double getWeight() {
        return locationScoreWeight;
    }

}
