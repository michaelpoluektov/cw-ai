package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLocationScore;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MiniBoard is a condensed version of the board class containing the minimum amount of data needed for the Ai. This is
 * done to reduce space in the decision tree and aims to speed the process up.
 */

public class MiniBoard {
    private final Integer round;
    private final Integer mrXLocation;
    private final ImmutableList<Integer> unmovedDetectiveLocations;
    private final ImmutableList<Integer> movedDetectiveLocations;
    private final GameSetup setup;
    private final Boolean mrXToMove;

    /**
     * @param board
     * Used to construct a {@link MiniBoard} for MrX from outside the class. We call the private constructor with true
     * for MrX to move.
     */

    public MiniBoard(Board board){
        this(board, board.getAvailableMoves().asList().get(0).source(), true);
        if(!(board.getAvailableMoves().asList().get(0).commencedBy() == Piece.MrX.MRX)) {
            throw new IllegalArgumentException("Passed board is detectives to move!");
        }
    }
    /**
     * @param board
     * Used in construction of a detectives {@link MiniBoard} from outside the class. It calls the private constructor
     * with MrX to move as false.
     */

    public MiniBoard(Board board, Integer location) {
        this(board, location, false);
        if(board.getAvailableMoves().asList().get(0).commencedBy() == Piece.MrX.MRX) {
            throw new IllegalArgumentException("Passed board is MRX to move!");
        }
    }

    /**
     * Used in {@link #advanceMrX(Integer)} to set a new miniBoard up for detectives to move.
     */
    private MiniBoard(Board board, Integer mrXLocation, Boolean mrXToMove) {
        this.mrXLocation = mrXLocation;
        if(!mrXToMove) this.unmovedDetectiveLocations = board.getAvailableMoves().stream()
                .map(Move::source)
                .distinct()
                .collect(ImmutableList.toImmutableList());
        else this.unmovedDetectiveLocations = ImmutableList.of();
        this.movedDetectiveLocations = board.getPlayers().stream()
                .filter(Piece::isDetective)
                .map(piece -> board.getDetectiveLocation((Piece.Detective) piece))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(detective -> !unmovedDetectiveLocations.contains(detective))
                .collect(ImmutableList.toImmutableList());
        this.setup = board.getSetup();
        this.mrXToMove = mrXToMove;
        this.round = board.getMrXTravelLog().size();
    }

    /**
     * Constructor is used in unchecked advance. This constructor must deal with advancing to both a MrX and detectives
     * MiniBoard
     * @param unmovedDetectiveLocations Location of detectives yet to move
     * @param movedDetectiveLocations Location of already moved detectives
     * @param mrXToMove True if its MrX's turn to move
     * @param round If this constructor is called and there a no more detectives left to move we need to increment the
     *              round by 1 in the new MiniBoard
     */

    private MiniBoard(Integer mrXLocation, 
                      ImmutableList<Integer> unmovedDetectiveLocations,
                      ImmutableList<Integer> movedDetectiveLocations,
                      GameSetup setup,
                      Boolean mrXToMove,
                      Integer round) {
        this.mrXLocation = mrXLocation;
        this.unmovedDetectiveLocations = unmovedDetectiveLocations;
        this.movedDetectiveLocations = movedDetectiveLocations;
        this.setup = setup;
        this.mrXToMove = mrXToMove;
        this.round = round;
    }
    public final Integer getMrXLocation() {
        return mrXLocation;
    }
    
    public final ImmutableList<Integer> getDetectiveLocations() {
        return Stream.concat(unmovedDetectiveLocations.stream(), movedDetectiveLocations.stream())
                .collect(ImmutableList.toImmutableList());
    }

    public final ImmutableList<Integer> getUnmovedDetectiveLocations() {
        return unmovedDetectiveLocations;
    }

    public final GameSetup getSetup() {
        return setup;
    }
    
    public final Boolean getMrXToMove() {
        return mrXToMove;
    }

    public final Integer getRound() {
        return round;
    }

    public ImmutableSet<Integer> getNodeDestinations(Integer source) {
        return ImmutableSet.copyOf(setup.graph.adjacentNodes(source).stream()
                .filter(node -> !getDetectiveLocations().contains(node))
                .collect(Collectors.toList()));
    }

    /**
     * Advances to a MiniBoard with detectives to move and increments the round. The moved and unmoved detective
     * lists are swapped so the advanced MiniBoard contains all detectives to move.
     * @throws UnsupportedOperationException If the MiniBoard has a winner
     * @return MiniBoard after MrX's move has been implemented
     */

    public MiniBoard advanceMrX(Integer destination) {
        if(getWinner() != winner.NONE) throw new UnsupportedOperationException("Can not advance board with winner!");
        if(!mrXToMove) {
            throw new IllegalArgumentException("Not MRX to move!");
        } else {
            return new MiniBoard(destination,
                    movedDetectiveLocations,
                    unmovedDetectiveLocations,
                    setup,
                    false,
                    round + 1);
        }
    }

    /**
     * Unchecked advance will advance a detective from source to destination and return a new MiniBoard. This does not
     * check whether this move is valid. The check should be done before the call to this method.
     */

    private MiniBoard uncheckedAdvance(Integer source, Integer destination) {
        final ArrayList<Integer> newUnmovedDetectiveLocation = new ArrayList<>(unmovedDetectiveLocations);
        newUnmovedDetectiveLocation.remove(source);
        final ArrayList<Integer> newMovedDetectiveLocation = new ArrayList<>(movedDetectiveLocations);
        newMovedDetectiveLocation.add(destination);
        return new MiniBoard(mrXLocation,
                ImmutableList.copyOf(newUnmovedDetectiveLocation),
                ImmutableList.copyOf(newMovedDetectiveLocation),
                setup,
                newUnmovedDetectiveLocation.isEmpty(),
                round);
    }

    /**
     * Advances a detective MiniBoard. We pass the source and destination of the move and validate this move
     * using {@link #getNodeDestinations(Integer)} which checks if the adjacent nodes contain the destination.
     * We can then call {@link #uncheckedAdvance(Integer, Integer)}
     * @throws UnsupportedOperationException If the move can not be made, either because there is not detective at,
     * the destination is occupied, there is no connection between source and destination or it is not the detectives'
     * turn to move.
     */
    public MiniBoard advanceDetective(Integer source, Integer destination) {
        if(!unmovedDetectiveLocations.contains(source)) {
            throw new UnsupportedOperationException("No movable detective there!");
        }
        if(!getNodeDestinations(source).contains(destination) || mrXToMove) {
            throw new UnsupportedOperationException("Illegal move!");
        }
        // Separate method created to not repeat same code for specific case in getAdvancedMiniBoard()
        return uncheckedAdvance(source, destination);
    }

    public enum winner {
        MRX,
        DETECTIVES,
        NONE
    }

    public final winner getWinner() {
        if(getDetectiveLocations().contains(mrXLocation) || getNodeDestinations(mrXLocation).size() == 0) {
            return winner.DETECTIVES;
        } else if(round == setup.rounds.size() && mrXToMove) return winner.MRX;
        else return winner.NONE;
    }

    /**
     * The class sums the total scores from all scoring objects and divides by the total weights.
     * @param scoringObjects We pass a variable number of {@link IntermediateScore} variables to the function. Examples
     *                       include {@link MrXLocationScore}. Each
     *                       Intermediate scoring object manages an individual heuristic scoring function.
     * @see IntermediateScore
     */
    public Double getMrXBoardScore(IntermediateScore... scoringObjects) {
        if(getWinner() == winner.DETECTIVES) return 0.0;
        if(getWinner() == winner.MRX) return Double.MAX_VALUE;
        double totalScore = 0.0;
        double totalWeights = 0.0;
        for(IntermediateScore scoringObject : scoringObjects) {
            totalWeights += scoringObject.getWeight();
            totalScore += scoringObject.getScore(this)*scoringObject.getWeight();
        }
        if(totalWeights == 0.0) throw new ArithmeticException("getTotalScore: All weights are zero");
        return totalScore/totalWeights;
    }

    /**
     * @return All possible MiniBoard that can be generated from the current agent's move. In case of detectives, we're
     * only considering the last unmoved detective.
     */
    public ImmutableSet<MiniBoard> getAdvancedMiniBoards() {
        if(mrXToMove) {
            return getNodeDestinations(mrXLocation).stream()
                    .map(this::advanceMrX)
                    .collect(ImmutableSet.toImmutableSet());
        }
        else {
            final Integer unmovedLocation = unmovedDetectiveLocations.get(unmovedDetectiveLocations.size() - 1);
            // Handles very specific case when detective is cornered by other detectives and can not make a move
            if(getNodeDestinations(unmovedLocation).isEmpty()) {
                return ImmutableSet.of(uncheckedAdvance(unmovedLocation, unmovedLocation));
            }
            else return getNodeDestinations(unmovedLocation).stream()
                    .map(destination -> advanceDetective(unmovedLocation, destination))
                    .collect(ImmutableSet.toImmutableSet());
        }
    }

    /**
     * Comparator by the scores of each MiniBoard using a predefined array of scoring function passed in the constructor
     */
    public static class ScoreComparator implements Comparator<MiniBoard> {
        private final IntermediateScore[] intermediateScores;
        public ScoreComparator(IntermediateScore... intermediateScores) {
            this.intermediateScores = intermediateScores;
        }
        @Override
        public int compare(MiniBoard o1, MiniBoard o2) {
            return Double.compare(o1.getMrXBoardScore(intermediateScores),
                    o2.getMrXBoardScore(intermediateScores));
        }
    }
}
