package uk.ac.bris.cs.scotlandyard.ui.ai.location;

import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class implements the {@link LocationPicker} interface, and implements the {@link #getBestDestination(ImmutableSet, Pair)}
 * method using the MiniMax algorithm, which iterates through the entire game tree N moves ahead of the given game
 * state, and expects each player to pick the best move they have available. It is optimised using Alpha-Beta pruning.
 * For more information, check out <a href="https://en.wikipedia.org/wiki/Minimax">MiniMax</a>,
 * <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">Alpha-Beta pruning</a>
 * <p>When the bottom of the game tree is reached, the algorithm calls the
 * {@link MiniBoard#getMrXBoardScore(IntermediateScore...)} method of the {@link MiniBoard} corresponding to a
 * state of the game after N moves with the provided {@link #intermediateScores} to evaluate the state.</p>
 * <p>The branching game states are generated using the {@link MiniBoard#getAdvancedMiniBoards()} method, which doesn't
 * take into account the different permutations of the order the detectives' moves, which enables us to significantly
 * improve the runtime of algorithm at little precision cost.</p>
 * @see LocationPicker
 * @see IntermediateScore
 * @see MiniBoard
 */

public class MiniMax implements LocationPicker {
    private final Board board;
    private final Toml constants;
    private final Integer steps;
    private final IntermediateScore[] intermediateScores;
    public MiniMax(Board board, Toml constants, Integer steps, IntermediateScore... intermediateScores) {
        this.board = board;
        this.constants = constants;
        this.steps = steps;
        this.intermediateScores = intermediateScores;
    }

    /**
     * This implementation runs the MiniMax algorithm on each individual branch separately, which prevents us from
     * optimising them with pruning, but enables us to compute their scores in parallel, which makes up for the
     * performance loss. This also allows us to manually input a set of destinations, which prevents MrX from picking an
     * illegal move and rank single and double moves separately.
     * @param destinations ImmutableSet of all available destinations
     * @return {@link Map.Entry} with the best destination and score based on the minimax algorithm.
     */

    @Nonnull
    @Override
    public Map.Entry<Integer, Double> getBestDestination(ImmutableSet<Integer> destinations,
                                                         Pair<Long, TimeUnit> simulationTime) {
        Map<Integer, Double> scoredDestinations = Collections.synchronizedMap(new HashMap<>());
        MiniBoard miniBoard = new MiniBoard(board);
        System.out.print("Rating destinations: ");
        destinations.parallelStream()
                .forEach(destination -> addRankedDestination(scoredDestinations, miniBoard, destination));
        Map.Entry<Integer, Double> bestEntry =
                Collections.max(scoredDestinations.entrySet(), Map.Entry.comparingByValue());
        System.out.println("\nBest destination is " + bestEntry.getKey() + " with score " + bestEntry.getValue());
        return bestEntry;
    }

    /**
     * @param map Mutable Map to be modified by the method, must be synchronised for parallel access.
     * @param miniBoard Source {@link MiniBoard} with MrX to move
     * @param destination Destination to be ranked
     */

    private void addRankedDestination(Map<Integer, Double> map, MiniBoard miniBoard, Integer destination) {
        Double score = runMiniMax(miniBoard.advanceMrX(destination),
                steps,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
        System.out.print(destination + " [" + String.format("%.2f", score) + "] ");
        map.put(destination, score);
    }

    /**
     * MiniMax is a recursive algorithm aiming to produce the most optimal move for an agent, in a game where
     * opponent(s) are trying to minimise his score. The recursion ends upon reaching a depth of 0, where the algorithm
     * outputs MrX's score of the provided {@link MiniBoard}. When the depth is greater than 0:
     * <p>If the board is MrX to move, the algorithm outputs the maximum output of the MiniMax algorithm ran on each
     * available move algorithm with a reduced depth.</p>
     * <p>If the board is detectives to move, the algorithm outputs the minimum output out of the recursive calls on the
     * detectives' available moves.</p>
     * @param miniBoard MiniBoard representing the current state
     * @param depth Depth to run the algorithm at, treats each detective move as a depth of 1 (i.e. 2 step lookahead for
     * a game with 5 detectives would be a depth of 5, since the first move of MrX is simulated in
     * {@link #getBestDestination(ImmutableSet, Pair)}).
     * @param alpha Alpha cut-off point for pruning
     * @param beta Beta cut-off point for pruning
     * @return The score of a given board after looking ahead 'depth' steps
     */

    private Double runMiniMax(MiniBoard miniBoard, Integer depth, Double alpha, Double beta){
        MiniBoard.winner winner = miniBoard.getWinner();
        if(winner == MiniBoard.winner.DETECTIVES) return 0.0;
        if(winner == MiniBoard.winner.MRX) return 1.0;
        if(depth == 0) return miniBoard.getMrXBoardScore(intermediateScores);
        Boolean mrXToMove = miniBoard.getMrXToMove();
        if(mrXToMove){
            double maxScore = Double.NEGATIVE_INFINITY;
            for(MiniBoard advancedMiniBoard : miniBoard.getAdvancedMiniBoards()) {
                Double advancedScore = runMiniMax(advancedMiniBoard, depth - 1, alpha, beta);
                if(maxScore < advancedScore) maxScore = advancedScore;
                alpha = Double.max(alpha, advancedScore);
                if(beta <= alpha) break;
            }
            return maxScore;
        }
        else{
            double minScore = Double.POSITIVE_INFINITY;
            for(MiniBoard advancedMiniBoard : miniBoard.getAdvancedMiniBoards()) {
                Double advancedScore = runMiniMax(advancedMiniBoard, depth - 1, alpha, beta);
                if(minScore > advancedScore) minScore = advancedScore;
                beta = Double.min(beta, advancedScore);
                if(beta <= alpha) break;
            }
            return minScore;
        }
    }
}
