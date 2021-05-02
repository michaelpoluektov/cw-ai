package uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class MrXLocationScore extends Dijkstra implements IntermediateScore {
    private final Integer location;
    private final ImmutableList<Integer> detectiveLocations;
    private final Double locationScoreExp;
    private final Double locationScoreWeight;
    public MrXLocationScore(MiniBoard miniBoard) {
        super(miniBoard);
        this.location = miniBoard.getMrXLocation();
        this.detectiveLocations = miniBoard.getDetectiveLocations();
        this.locationScoreExp = miniBoard.getConstants().getDouble("location.exp");
        this.locationScoreWeight = miniBoard.getConstants().getDouble("location.weight");
    }

    @Nonnull
    @Override
    public Double getScore() {
        ImmutableList<Integer> mrXLocationList = ImmutableList.of(location);
        List<Integer> distanceToSource = getDistances(mrXLocationList, detectiveLocations).get(0);
        Collections.sort(distanceToSource);
        List<Double> weightsList = distanceToSource.stream()
                .map(value -> pow(locationScoreExp, -value)/(1 - pow(locationScoreExp, -1)))
                .collect(Collectors.toList());
        Double weightsListSum = weightsList.stream().reduce(0.0,Double::sum);
        weightsList = weightsList.stream().map(value -> value/weightsListSum).collect(Collectors.toList());


        double totalScore = 0.0;
        for(int i = 0; i < distanceToSource.size(); i++) {
            Double unweightedScore = 1 - pow(locationScoreExp, 1 - distanceToSource.get(i));
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
