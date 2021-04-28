package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.MrXLocationScore;

public class MyAi implements Ai {
	private final Toml constants = new Toml().read(getClass().getResourceAsStream("/constants.toml"));
	@Nonnull @Override public String name() { return "MrX one step lookahead"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		Move random = moves.get(new Random().nextInt(moves.size()));
		int destination = getMoveDestination(random);
		MrXLocationScore scoringObject = new MrXLocationScore(constants, board, destination);
		System.out.println(scoringObject.getScore());
		return random;
	}
	public int getMoveDestination(Move move) {
		return move.visit(new Move.Visitor<Integer>() {
			@Override
			public Integer visit(Move.SingleMove singleMove) {
				return singleMove.destination;
			}

			@Override
			public Integer visit(Move.DoubleMove doubleMove) {
				return doubleMove.destination2;
			}
		});
	}
}
