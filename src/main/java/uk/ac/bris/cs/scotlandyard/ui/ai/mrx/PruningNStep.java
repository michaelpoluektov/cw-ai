package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.LocationPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.location.MiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.DefaultTicketPicker;
import uk.ac.bris.cs.scotlandyard.ui.ai.ticket.TicketPicker;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PruningNStep implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    private final Double doubleThreshold = constants.getDouble("double.threshold");
    private final Double doubleOffset = constants.getDouble("double.minOffset");
    @Nonnull
    @Override public String name() { return "MrX pruning N-step lookahead - deterministic"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        LocationPicker miniMax = new MiniMax(board, constants, 6);
        TicketPicker defaultPicker = new DefaultTicketPicker(board);
        ImmutableSet<Move> availableMoves = board.getAvailableMoves();
        ImmutableSet<Integer> singleMoveDestinations = getSingleMoveDestinations(availableMoves);
        Map.Entry<Integer, Double> bestSingleEntry = miniMax.getBestDestination(singleMoveDestinations);
        Double bestSingleValue = bestSingleEntry.getValue();
        if(bestSingleValue < doubleThreshold){
            ImmutableSet<Integer> doubleMoveDestinations = getDoubleMoveDestinations(availableMoves);
            if(!doubleMoveDestinations.isEmpty()) {
                Map.Entry<Integer, Double> bestDoubleEntry = miniMax.getBestDestination(doubleMoveDestinations);
                Double bestDoubleValue = bestDoubleEntry.getValue();
                if(bestDoubleValue > bestSingleValue + doubleOffset || bestSingleValue == 0) {
                    ImmutableSet<Move> doubleMoves = getDoubleMovesWithDestination(availableMoves, bestDoubleEntry.getKey());
                    return defaultPicker.getBestMove(doubleMoves);
                }
            }
        }
        ImmutableSet<Move> singleMoves = getSingleMovesWithDestination(availableMoves, bestSingleEntry.getKey());
        return defaultPicker.getBestMove(singleMoves);
    }

    private ImmutableSet<Integer> getSingleMoveDestinations(ImmutableSet<Move> moves) {
        return moves.stream()
                .filter(move -> move instanceof Move.SingleMove)
                .map(move -> ((Move.SingleMove) move).destination)
                .distinct()
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Integer> getDoubleMoveDestinations(ImmutableSet<Move> moves) {
        return moves.stream()
                .filter(move -> move instanceof Move.DoubleMove)
                .map(move -> ((Move.DoubleMove) move).destination2)
                .distinct()
                .filter(destination -> !getSingleMoveDestinations(moves).contains(destination))
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Move> getSingleMovesWithDestination(ImmutableSet<Move> allMoves, Integer destination) {
        return allMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .filter(move -> move instanceof Move.SingleMove)
                .collect(ImmutableSet.toImmutableSet());
    }

    private ImmutableSet<Move> getDoubleMovesWithDestination(ImmutableSet<Move> allMoves, Integer destination) {
        return allMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .collect(ImmutableSet.toImmutableSet());
    }
}
