package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PruningNStep implements Ai {
    private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
    @Nonnull
    @Override public String name() { return "MrX Pruning N-step lookahead - deterministic"; }

    @Nonnull @Override public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        // returns a random move, replace with your own implementation
        ImmutableList<Move> singleMoves = board.getAvailableMoves().stream()
                .filter(move -> move instanceof Move.SingleMove)
                .collect(ImmutableList.toImmutableList());

        Map.Entry<Move,Double> bestSingleEntry = getBestMove(singleMoves, board);
        System.out.println("Best single move score: "+bestSingleEntry.getValue());
        if(bestSingleEntry.getValue() < constants.getDouble("double.threshold")){
            ImmutableList<Move> doubleMoves = board.getAvailableMoves().stream()
                    .filter(move -> move instanceof Move.DoubleMove)
                    .collect(ImmutableList.toImmutableList());
            Map.Entry<Move, Double> bestDoubleEntry = getBestMove(doubleMoves, board);
            if(bestDoubleEntry.getValue() > bestSingleEntry.getValue() + constants.getDouble("double.minOffset")) {
                System.out.println("Best double move score: "+bestDoubleEntry.getValue());
                return bestDoubleEntry.getKey();
            }
        }
        return bestSingleEntry.getKey();
    }
    private Map.Entry<Move, Double> getBestMove(ImmutableList<Move> moves, Board board){
        HashMap<Move, Double> scoredMoves = new HashMap<>();
        for(Move move : moves) {
            System.out.println("Rated one possibility.");
            Integer moveDestination = move.visit(new MoveDestinationVisitor());
            Board advancedBoard = ((Board.GameState) board).advance(move);
            MiniBoard advancedMiniBoard = new MiniBoard(advancedBoard, moveDestination, constants);
            scoredMoves.put(move, MiniMax(advancedMiniBoard, 6, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        }
        return Collections.max(scoredMoves.entrySet(), Map.Entry.comparingByValue());
    }
    private Double MiniMax(MiniBoard miniBoard, Integer depth, Double alpha, Double beta){
        Boolean mrXToMove = miniBoard.getMrXToMove();
        if(depth == 0) return miniBoard.getMrXBoardScore(ScoringClassEnum.MRXAVAILABLEMOVES,
                ScoringClassEnum.MRXLOCATION);
        if(miniBoard.getWinner() == MiniBoard.winner.MRX) return 1.0;
        else if(miniBoard.getWinner() == MiniBoard.winner.DETECTIVES) return 0.0;
        if(mrXToMove){
            double maxScore = Double.NEGATIVE_INFINITY;
            for(Integer destination : miniBoard.getNodeDestinations(miniBoard.getMrXLocation())) {
                Double advancedScore = MiniMax(miniBoard.advanceMrX(destination), depth -1, alpha, beta);
                if(maxScore < advancedScore) maxScore = advancedScore;
                alpha = Double.max(alpha, advancedScore);
                if(beta <= alpha) break;
            }
            return maxScore;
        }
        else{
            double minScore = Double.POSITIVE_INFINITY;
            for(MiniBoard advancedBoard : detectiveAdvancedBoards(miniBoard, miniBoard.getDetectiveLocations())) {
                Double advancedScore = MiniMax(advancedBoard, depth - 1, alpha, beta);
                if(minScore > advancedScore) minScore = advancedScore;
                beta = Double.min(beta, advancedScore);
                if(beta >= alpha) break;
            }
            return minScore;
        }
    }

    private ImmutableList<MiniBoard> detectiveAdvancedBoards(MiniBoard miniBoard,
                                                             ImmutableList<Integer> unmovedDetectives) {
        List<MiniBoard> returnedList = new ArrayList<>();
        List<Integer> newUnmovedDetectives = new ArrayList<>(unmovedDetectives);
        if (unmovedDetectives.isEmpty()) {
            returnedList.add(miniBoard);
        } else {
            ImmutableList<MiniBoard> advancedMiniBoards = miniBoard.getNodeDestinations(newUnmovedDetectives.get(0))
                    .stream()
                    .map(destination -> miniBoard.advanceDetective(newUnmovedDetectives.get(0),
                            destination,
                            newUnmovedDetectives.size() == 1))
                    .collect(ImmutableList.toImmutableList());
            newUnmovedDetectives.remove(0);
            for (MiniBoard advancedMiniBoard : advancedMiniBoards)
                returnedList.addAll(detectiveAdvancedBoards(advancedMiniBoard,
                        ImmutableList.copyOf(newUnmovedDetectives)));
        }
        return ImmutableList.copyOf(returnedList);
    }

	/*private Double Maximise(MiniBoard miniBoard, Integer depth) {
		if (!miniBoard.getMrXToMove()) throw new IllegalArgumentException("Passed board is not MrX to move!");
		if(depth == 0) return miniBoard.getMrXBoardScore(ScoringClassEnum.MRXAVAILABLEMOVES,
				ScoringClassEnum.MRXLOCATION);
		double maxScore = 0.0;
		for(Integer destination : miniBoard.getNodeDestinations(miniBoard.getMrXLocation())) {
			Double advancedScore = Minimise(miniBoard.advanceMrX(destination), depth - 1);
			if(maxScore < advancedScore) maxScore = advancedScore;
		}
		return maxScore;
	}

	private Double Minimise(MiniBoard miniBoard, Integer depth) {
		if (miniBoard.getMrXToMove()) throw new IllegalArgumentException("Passed board is MrX to move!");
		if (depth == 0) return miniBoard.getMrXBoardScore(ScoringClassEnum.MRXAVAILABLEMOVES,
				ScoringClassEnum.MRXLOCATION);
		double minScore = 0.0;
		for(Integer destination : miniBoard.getNodeDestinations(miniBoard.getDetectiveLocations().get(0))) {
			Double advancedScore = Maximise(miniBoard.advanceMrX(destination), depth - 1);
			if(minScore > advancedScore) minScore = advancedScore;
		}
		return minScore;
	}*/
}
