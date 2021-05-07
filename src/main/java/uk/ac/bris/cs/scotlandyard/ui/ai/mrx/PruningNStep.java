package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PruningNStep implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "MrX pruning N-step lookahead - deterministic"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        ImmutableSet<Move> availableMoves = board.getAvailableMoves();
        ImmutableSet<Integer> singleMoveDestinations = getSingleMoveDestinations(availableMoves);
        Map.Entry<Integer,Double> bestSingleEntry = getBestDestination(singleMoveDestinations, board);
        Move bestSingleMove = getBestMove(getMovesWithDestination(availableMoves, bestSingleEntry.getKey()), board);
        /*if(bestSingleEntry.getValue() < constants.getDouble("double.threshold")){
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
        }*/
        return bestSingleMove;
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

    private Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations, Board board) {
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        MiniBoard miniBoard = new MiniBoard(board, constants);
        System.out.print("Rating destinations: ");
        for(Integer destination : destinations) {
            System.out.print(destination + " ");
            MiniBoard advancedMiniBoard = miniBoard.advanceMrX(destination);
            scoredDestinations.put(destination,
                    MiniMax(advancedMiniBoard, 5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        }
        Map.Entry<Integer, Double> bestEntry =
                Collections.max(scoredDestinations.entrySet(), Map.Entry.comparingByValue());
        System.out.println("\nBest destination is " + bestEntry.getKey() + " with score " + bestEntry.getValue());
        return bestEntry;
    }

    private ImmutableSet<Move> getMovesWithDestination(ImmutableSet<Move> allMoves, Integer destination) {
        return allMoves.stream()
                .filter(move -> move.visit(new MoveDestinationVisitor()).equals(destination))
                .filter(move -> move instanceof Move.SingleMove)
                .collect(ImmutableSet.toImmutableSet());
    }

    public Optional<Move> getBestNonSecretMove(ImmutableSet<Move> movesWithDestinations, Board.TicketBoard ticketBoard) {
        return movesWithDestinations.asList()
                .stream()
                .filter(move -> !Iterables.contains(move.tickets(), ScotlandYard.Ticket.SECRET))
                .max(Comparator.comparingInt(move -> ticketBoard.getCount(move.tickets().iterator().next())));
    }

    public Optional<Move> getSecretMove(ImmutableSet<Move> movesWithDestination) {
        return movesWithDestination.stream()
                .filter(move -> Iterables.contains(move.tickets(), ScotlandYard.Ticket.SECRET))
                .findAny();
    }

    private Move getBestMove(ImmutableSet<Move> movesWithDestination, Board board) {
        Board.TicketBoard mrXTicketBoard = board.getPlayerTickets(Piece.MrX.MRX).orElseThrow();
        Boolean isReveal;
        try {
            isReveal = board.getSetup().rounds.get(board.getMrXTravelLog().size() - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            isReveal = false;
        }
        if(isReveal) {
            return getSecretMove(movesWithDestination)
                    .orElseGet(() -> getBestNonSecretMove(movesWithDestination, mrXTicketBoard).orElseThrow());
        } else return getBestNonSecretMove(movesWithDestination, mrXTicketBoard)
                .orElseGet(() -> getSecretMove(movesWithDestination).orElseThrow());
    }

    /*private Map.Entry<Move, Double> getBestMove(ImmutableList<Move> moves, Board board){
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
    }*/

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
