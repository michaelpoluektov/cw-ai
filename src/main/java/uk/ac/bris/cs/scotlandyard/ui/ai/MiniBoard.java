package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MiniBoard {
    private final Integer round;
    private final Integer mrXLocation;
    private final ImmutableList<Integer> unmovedDetectiveLocations;
    private final ImmutableList<Integer> movedDetectiveLocations;
    private final GameSetup setup;
    private final Boolean mrXToMove;
    private final Toml constants;

    public MiniBoard(Board board, Toml constants){
        this(board, constants, board.getAvailableMoves().asList().get(0).source(), true);
        if(!(board.getAvailableMoves().asList().get(0).commencedBy() == Piece.MrX.MRX)) {
            throw new IllegalArgumentException("Passed board is detectives to move!");
        }
    }

    public MiniBoard(Board board, Integer location, Toml constants) {
        this(board, constants, location, false);
        if(board.getAvailableMoves().asList().get(0).commencedBy() == Piece.MrX.MRX) {
            throw new IllegalArgumentException("Passed board is MRX to move!");
        }
    }

    private MiniBoard(Board board, Toml constants, Integer mrXLocation, Boolean mrXToMove) {
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
        this.constants = constants;
    }

    private MiniBoard(Integer mrXLocation, 
                      ImmutableList<Integer> unmovedDetectiveLocations,
                      ImmutableList<Integer> movedDetectiveLocations,
                      GameSetup setup,
                      Boolean mrXToMove,
                      Integer round,
                      Toml constants) {
        this.mrXLocation = mrXLocation;
        this.unmovedDetectiveLocations = unmovedDetectiveLocations;
        this.movedDetectiveLocations = movedDetectiveLocations;
        this.setup = setup;
        this.mrXToMove = mrXToMove;
        this.round = round;
        this.constants = constants;
    }
    public Integer getMrXLocation() {
        return mrXLocation;
    }
    
    public ImmutableList<Integer> getDetectiveLocations() {
        return Stream.concat(unmovedDetectiveLocations.stream(), movedDetectiveLocations.stream())
                .collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Integer> getUnmovedDetectiveLocations() {
        return unmovedDetectiveLocations;
    }

    public GameSetup getSetup() {
        return setup;
    }
    
    public Boolean getMrXToMove() {
        return mrXToMove;
    }

    public Integer getRound() {
        return round;
    }

    public Toml getConstants() {
        return constants;
    }
    
    public ImmutableSet<Integer> getNodeDestinations(Integer source) {
        return ImmutableSet.copyOf(setup.graph.adjacentNodes(source).stream()
                .filter(node -> !getDetectiveLocations().contains(node))
                .collect(Collectors.toList()));
    }

    public MiniBoard advanceMrX(Integer destination) {
        if(getWinner() != winner.NONE) throw new UnsupportedOperationException("Can not advance board with winner!");
        if(!mrXToMove) {
            throw new IllegalArgumentException("Not MRX to move!");
        } else {
            // IMPORTANT: moved and unmoved locations are swapped
            return new MiniBoard(destination,
                    movedDetectiveLocations,
                    unmovedDetectiveLocations,
                    setup,
                    false,
                    round + 1,
                    constants);
        }
    }

    private MiniBoard uncheckedAdvance(Integer source, Integer destination) {
        ArrayList<Integer> newUnmovedDetectiveLocation = new ArrayList<>(unmovedDetectiveLocations);
        newUnmovedDetectiveLocation.remove(source);
        ArrayList<Integer> newMovedDetectiveLocation = new ArrayList<>(movedDetectiveLocations);
        newMovedDetectiveLocation.add(destination);
        return new MiniBoard(mrXLocation,
                ImmutableList.copyOf(newUnmovedDetectiveLocation),
                ImmutableList.copyOf(newMovedDetectiveLocation),
                setup,
                newUnmovedDetectiveLocation.isEmpty(),
                round,
                constants);
    }

    public MiniBoard advanceDetective(Integer source, Integer destination) {
        if(!unmovedDetectiveLocations.contains(source)) {
            throw new IllegalArgumentException("No movable detective there!");
        }
        if(!getNodeDestinations(source).contains(destination) || mrXToMove) {
            throw new IllegalArgumentException("Illegal move!");
        }
        // Separate method created to not repeat same code for specific case in getAdvancedMiniBoard()
        return uncheckedAdvance(source, destination);
    }

    public enum winner {
        MRX,
        DETECTIVES,
        NONE
    }

    public winner getWinner() {
        if(getDetectiveLocations().contains(mrXLocation) || getNodeDestinations(mrXLocation).size() == 0) {
            return winner.DETECTIVES;
        } else if(round == setup.rounds.size() && mrXToMove) return winner.MRX;
        else return winner.NONE;
    }

    public Double getMrXBoardScore(ScoringClassEnum... scoringClasses) {
        if(getWinner() == winner.DETECTIVES) return 0.0;
        if(getWinner() == winner.MRX) return 1.0;
        double totalScore = 0.0;
        double totalWeights = 0.0;
        for(ScoringClassEnum scoringClass : scoringClasses) {
            IntermediateScore scoringObject = scoringClass.getScoringObject(this);
            totalWeights += scoringObject.getWeight();
            totalScore += scoringObject.getScore()*scoringObject.getWeight();
        }
        if(totalWeights == 0.0) throw new ArithmeticException("getTotalScore: All weights are zero");
        return totalScore/totalWeights;
    }
    public ImmutableSet<MiniBoard> getAdvancedMiniBoards() {
        if(mrXToMove) {
            return getNodeDestinations(mrXLocation).stream()
                    .map(this::advanceMrX)
                    .collect(ImmutableSet.toImmutableSet());
        }
        else {
            Integer unmovedLocation = unmovedDetectiveLocations.get(unmovedDetectiveLocations.size() - 1);
            // Handles very specific case when detective is cornered by other detectives and can not make a move
            if(getNodeDestinations(unmovedLocation).isEmpty()) {
                return ImmutableSet.of(uncheckedAdvance(unmovedLocation, unmovedLocation));
            }
            else return getNodeDestinations(unmovedLocation).stream()
                    .map(destination -> advanceDetective(unmovedLocation, destination))
                    .collect(ImmutableSet.toImmutableSet());
        }
    }
}
