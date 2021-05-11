package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class MrXLocationScore extends Dijkstra implements IntermediateScore {
    private final Double locationScoreExp;
    private final Double locationScoreWeight;
    public MrXLocationScore(Toml constants) {
        this.locationScoreExp = constants.getDouble("location.exp");
        this.locationScoreWeight = constants.getDouble("location.weight");
    }
    @Nonnull
    @Override
    public Double getScore(MiniBoard miniBoard) {
        final ImmutableList<Integer> mrXLocationList = ImmutableList.of(miniBoard.getMrXLocation());
        final List<Integer> distanceToSource =
                getDistances(mrXLocationList, miniBoard.getDetectiveLocations(), miniBoard).get(0);
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
