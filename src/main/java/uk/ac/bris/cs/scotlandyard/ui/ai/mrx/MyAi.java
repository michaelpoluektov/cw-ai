package uk.ac.bris.cs.scotlandyard.ui.ai.mrx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.LocationScore;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "MrX one step lookahead"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		var graph = board.getSetup().graph;
		var mrXLocation = ImmutableList.of(board.getAvailableMoves().asList().get(0).source());
		List<Integer> detectivesLocation = new ArrayList<>();
		for(Piece piece : board.getPlayers()) {
			if(piece.isDetective()) detectivesLocation.add(board.getDetectiveLocation((Piece.Detective) piece).get());
		}
		ArrayList<Integer> store = new ArrayList<>();
		Move random = moves.get(new Random().nextInt(moves.size()));
		int destination = getMoveDestination(random);
		store.add(destination);
		LocationScore idk = new LocationScore(graph, ImmutableList.copyOf(store), ImmutableList.copyOf(detectivesLocation));
		System.out.println(idk.getMrXScore());
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
