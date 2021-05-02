package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class MiniBoard {
    private final Integer round;
    private final Integer mrXLocation;
    private final ImmutableList<Integer> detectiveLocations;
    private final GameSetup setup;
    private final Boolean mrXToMove;
    private final Toml constants;
    public MiniBoard(Board boardWithMrXToMove, Toml constants) {
        this(boardWithMrXToMove, boardWithMrXToMove.getAvailableMoves().asList().get(0).source(), constants);
        if(boardWithMrXToMove.getAvailableMoves().asList().get(0).commencedBy() != Piece.MrX.MRX) {
            throw new IllegalArgumentException("Passed board is not MRX to move!");
        }
    }
    public MiniBoard(Board board, Integer location, Toml constants) {
        this.mrXLocation = location;
        this.detectiveLocations = board.getPlayers().stream()
                .filter(Piece::isDetective)
                .map(piece -> board.getDetectiveLocation((Piece.Detective) piece))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList());
        this.setup = board.getSetup();
        this.mrXToMove = false;
        // Actual round not used, reveals and game end based on log size
        this.round = board.getMrXTravelLog().size();
        this.constants = constants;
    }
    private MiniBoard(Integer mrXLocation, 
                      ImmutableList<Integer> detectiveLocations, 
                      GameSetup setup,
                      Boolean mrXToMove,
                      Integer round,
                      Toml constants) {
        this.mrXLocation = mrXLocation;
        this.detectiveLocations = detectiveLocations;
        this.setup = setup;
        this.mrXToMove = mrXToMove;
        this.round = round;
        this.constants = constants;
    }
    public Integer getMrXLocation() {
        return mrXLocation;
    }
    
    public ImmutableList<Integer> getDetectiveLocations() {
        return detectiveLocations;
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
                .filter(node -> !detectiveLocations.contains(node))
                .collect(Collectors.toList()));
    }

    public MiniBoard advanceMrX(Integer destination) {
        if(!getNodeDestinations(mrXLocation).contains(destination) || !mrXToMove) {
            throw new IllegalArgumentException("Illegal move!");
        } else {
            return new MiniBoard(destination, detectiveLocations, setup, false, round, constants);
        }
    }

    public MiniBoard advanceDetective(Integer source, Integer destination, Boolean isLastDetective) {
        if(!detectiveLocations.contains(source)) throw new IllegalArgumentException("No detective there!");
        if(!getNodeDestinations(source).contains(destination) || mrXToMove) {
            throw new IllegalArgumentException("Illegal move!");
        }
        ArrayList<Integer> newDetectiveLocation = new ArrayList<>(detectiveLocations);
        newDetectiveLocation.set(newDetectiveLocation.indexOf(source), destination);
        return new MiniBoard(mrXLocation,
                ImmutableList.copyOf(newDetectiveLocation),
                setup,
                isLastDetective,
                round,
                constants);
    }

    public Double getBoardScore(ScoringClassEnum... scoringClasses) {
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
}
