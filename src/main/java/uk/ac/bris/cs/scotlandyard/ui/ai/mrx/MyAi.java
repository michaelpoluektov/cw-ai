package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.ScoringClassEnum;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXAvailableMovesScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLocationScore;

public class MyAi implements Ai {
	private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
	@Nonnull @Override public String name() { return "MrX one step lookahead - deterministic"; }

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
			Integer moveDestination = move.visit(new MoveDestinationVisitor());
			Board advancedBoard = ((Board.GameState) board).advance(move);
			MiniBoard advancedMiniBoard = new MiniBoard(advancedBoard, moveDestination, constants);
			scoredMoves.put(move, advancedMiniBoard.getBoardScore(ScoringClassEnum.MRXAVAILABLEMOVES,
					ScoringClassEnum.MRXLOCATION));
		}
		return Collections.max(scoredMoves.entrySet(), Map.Entry.comparingByValue());
	}
}
