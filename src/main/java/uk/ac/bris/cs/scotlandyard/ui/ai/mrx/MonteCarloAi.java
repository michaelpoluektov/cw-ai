
package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo.MCNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo.MCTree;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MonteCarloAi implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "MrX pruning Monte Carlo lookahead - deterministic"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        ImmutableSet<Move> availableMoves = board.getAvailableMoves();
        ImmutableSet<Integer> singleMoveDestinations = getSingleMoveDestinations(availableMoves);
        Map.Entry<Integer,Double> bestSingleEntry = getBestDestination(board, timeoutPair);
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

    private Map.Entry<Integer, Double> getBestDestination( Board board,  Pair<Long, TimeUnit> timeoutPair) {
        HashMap<Integer, Double> scoredDestinations = new HashMap<>();
        MCTree tree = new MCTree(board, constants);
        long timeLeft = timeoutPair.right().toMillis(timeoutPair.left());
        Integer numberOfSimulation = 0;
        while(timeLeft >300){
            long startTime = System.currentTimeMillis();
            tree.singleSimulation();
            numberOfSimulation ++;
            long endTime = System.currentTimeMillis();
            timeLeft = timeLeft - (endTime - startTime);
        }
        System.out.println(numberOfSimulation);
        for(MCNode child : tree.getRootNode().getChildren()){
            scoredDestinations.put(child.getMiniBoard().getMrXLocation(), child.UTCScore());
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


}
