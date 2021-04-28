package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class MrXLocationScore extends LocationScore implements Score{
    private final Integer location;
    private final ImmutableList<Integer> detectiveLocations;
    private final Double locationScoreExp;
    public MrXLocationScore(Toml constants, Board board, Integer location) {
        super(board);
        this.location = location;
        this.detectiveLocations = board.getPlayers().stream()
                .filter(Piece::isDetective)
                .map(piece -> board.getDetectiveLocation((Piece.Detective) piece))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList());
        this.locationScoreExp = constants.getDouble("location.exp");
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
}
