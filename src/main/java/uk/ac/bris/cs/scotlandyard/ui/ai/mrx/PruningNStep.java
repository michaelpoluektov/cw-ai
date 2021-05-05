package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PruningNStep implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "MrX pruning N-step lookahead - deterministic"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        ImmutableList<Move> singleMoves = ImmutableList.copyOf(board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.SingleMove)
                .collect(Collectors.toMap(move -> ((Move.SingleMove) move).destination, Function.identity(),
                        (move1, move2) -> move1))
                .values());

        Map.Entry<Move,Double> bestSingleEntry = getBestMove(singleMoves, board);
        System.out.println("Best single move score: "+bestSingleEntry.getValue());
        if(bestSingleEntry.getValue() < constants.getDouble("double.threshold")){
            ImmutableList<Move> doubleMoves = ImmutableList.copyOf(board.getAvailableMoves().stream()
                    .filter(move -> move instanceof Move.DoubleMove)
                    .collect(Collectors.toMap(move -> ((Move.DoubleMove) move).destination2, Function.identity(),
                            (move1, move2) -> move1))
                    .values());
            if(doubleMoves.isEmpty()) return bestSingleEntry.getKey();
            Map.Entry<Move, Double> bestDoubleEntry = getBestMove(doubleMoves, board);
            if(bestDoubleEntry.getValue() > bestSingleEntry.getValue() + constants.getDouble("double.minOffset") ||
                    bestSingleEntry.getValue() == 0) {
                System.out.println("Best double move score: "+bestDoubleEntry.getValue());
                return bestDoubleEntry.getKey();
            }
        }
        return bestSingleEntry.getKey();
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
                .filter(destination -> getSingleMoveDestinations(moves).contains(destination))
                .collect(ImmutableSet.toImmutableSet());
    }

    private Map.Entry<Move, Double> getBestMove(ImmutableList<Move> moves, Board board){
        HashMap<Move, Double> scoredMoves = new HashMap<>();
        for(Move move : moves) {
            System.out.println("Rating move: "+move);
            Integer moveDestination = move.visit(new MoveDestinationVisitor());
            Board advancedBoard = ((Board.GameState) board).advance(move);
            MiniBoard advancedMiniBoard = new MiniBoard(advancedBoard, moveDestination, constants);
            scoredMoves.put(move, MiniMax(advancedMiniBoard, 5,
                    Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        }
        return Collections.max(scoredMoves.entrySet(), Map.Entry.comparingByValue());
    }

    private Double MiniMax(MiniBoard miniBoard, Integer depth, Double alpha, Double beta){
        MiniBoard.winner winner = miniBoard.getWinner();
        if(winner == MiniBoard.winner.DETECTIVES) return 0.0;
        if(winner == MiniBoard.winner.MRX) return 1.0;
        if(depth == 0) return miniBoard.getMrXBoardScore(ScoringClassEnum.MRXLOCATION,
                ScoringClassEnum.MRXAVAILABLEMOVES);
        Boolean mrXToMove = miniBoard.getMrXToMove();
        if(mrXToMove){
            double maxScore = Double.NEGATIVE_INFINITY;
            for(Integer destination : miniBoard.getNodeDestinations(miniBoard.getMrXLocation())) {
                Double advancedScore = MiniMax(miniBoard.advanceMrX(destination), depth - 1, alpha, beta);
                if(maxScore < advancedScore) maxScore = advancedScore;
                alpha = Double.max(alpha, advancedScore);
                if(beta <= alpha) break;
            }
            return maxScore;
        }
        else{
            double minScore = Double.POSITIVE_INFINITY;
            Integer source = miniBoard.getDetectiveLocations().get(miniBoard.getUnmovedDetectiveLocations().size()-1);
            for(Integer destination : miniBoard.getNodeDestinations(source)) {
                Double advancedScore = MiniMax(miniBoard.advanceDetective(source, destination),
                        depth - 1,
                        alpha,
                        beta);
                if(minScore > advancedScore) minScore = advancedScore;
                beta = Double.min(beta, advancedScore);
                if(beta <= alpha) break;
            }
            return minScore;
        }
    }
}
