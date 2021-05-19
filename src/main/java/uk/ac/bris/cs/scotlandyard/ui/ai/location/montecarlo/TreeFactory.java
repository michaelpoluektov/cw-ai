package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

public class TreeFactory {
    public static TreeSimulation<HeavyNode> newHeavyTree(Board board,
                                                         Toml constants,
                                                         IntermediateScore... intermediateScores) {
        final HeavyNode rootNode = new HeavyNode(new MiniBoard(board),
                null,
                constants.getDouble("", 3.0));
        return new TreeSimulation<>(constants, rootNode, intermediateScores);
    }
    public static TreeSimulation<LightNode> newLightTree(Board board,
                                                         Toml constants,
                                                         IntermediateScore... intermediateScores) {
        final LightNode rootNode = new LightNode(new MiniBoard(board),
                null,
                constants.getDouble("", 3.0));
        return new TreeSimulation<>(constants, rootNode, intermediateScores);
    }
}
