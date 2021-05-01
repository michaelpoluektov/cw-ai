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
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveDestinationVisitor;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.StateScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXAvailableMovesScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXLocationScore;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.mrxstate.MrXTicketScore;

public class MyAi implements Ai {
	private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
	@Nonnull @Override public String name() { return "MrX one step lookahead - deterministic"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		ImmutableList<Move> moves = board.getAvailableMoves().stream()
				.filter(move -> move instanceof Move.SingleMove)
				.collect(ImmutableList.toImmutableList());
		HashMap<Move, Double> scoredMoves = new HashMap<>();
		for(Move move : moves) {
			List<IntermediateScore> intermediateScores = new ArrayList<>(2);
			Integer moveDestination = move.visit(new MoveDestinationVisitor());
			intermediateScores.add(new MrXLocationScore(constants, board, moveDestination));
			intermediateScores.add(new MrXAvailableMovesScore(constants, board));
			intermediateScores.add(new MrXTicketScore(constants, board));
			scoredMoves.put(move, StateScore.getTotalScore(intermediateScores));
		}
		Map.Entry maxScore = Collections.max(scoredMoves.entrySet(), Map.Entry.comparingByValue());
		System.out.println("Move score: "+maxScore.getValue());
		return (Move) maxScore.getKey();
	}
}
