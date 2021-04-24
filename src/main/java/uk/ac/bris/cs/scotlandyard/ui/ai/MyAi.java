package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableList;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Name me!"; }

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
		LocationScore idk = new LocationScore(graph, mrXLocation, ImmutableList.copyOf(detectivesLocation));
		System.out.println(idk.getMrXScore());
		return moves.get(new Random().nextInt(moves.size()));
	}
}
