package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MiniBoard {
    private final Integer round;
    private final Integer mrXLocation;
    private final ImmutableList<Integer> detectiveLocations;
    private final GameSetup setup;
    private final Boolean mrXToMove;
    public MiniBoard(Board boardWithMrXToMove) {
        this(boardWithMrXToMove, boardWithMrXToMove.getAvailableMoves().asList().get(0).source());
        if(boardWithMrXToMove.getAvailableMoves().asList().get(0).commencedBy() != Piece.MrX.MRX) {
            throw new IllegalArgumentException("Passed board is not MRX to move!");
        }
    }
    public MiniBoard(Board board, Integer location) {
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
    }
    private MiniBoard(Integer mrXLocation, 
                      ImmutableList<Integer> detectiveLocations, 
                      GameSetup setup,
                      Boolean mrXToMove,
                      Integer round) {
        this.mrXLocation = mrXLocation;
        this.detectiveLocations = detectiveLocations;
        this.setup = setup;
        this.mrXToMove = mrXToMove;
        this.round = round;
    }
    public Integer getMrXLocation() {
        return mrXLocation;
    }
    
    public ImmutableList<Integer> getDetectiveLocations() {
        return detectiveLocations;
    }
    
    public Boolean getMrXToMove() {
        return mrXToMove;
    }

    public Integer getRound() {
        return round;
    }
    
    public ImmutableSet<Integer> getMrXAdjacentNodes() {
        return ImmutableSet.copyOf(setup.graph.adjacentNodes(mrXLocation));
    }

    public ImmutableSet<Integer> getDetectivesAdjacentNodes() {
        final Set<Integer> adjacentNodes = new HashSet<>();
        for(Integer location : detectiveLocations) {
            adjacentNodes.addAll(setup.graph.adjacentNodes(location));
        }
        return ImmutableSet.copyOf(adjacentNodes);
    }
}
