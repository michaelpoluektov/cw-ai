package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;

public class TreeFactory {
    public static TreeSimulation<HeavyNode> newHeavyTree(Board board,
                                                         Toml constants,
                                                         Integer coreNumber,
                                                         IntermediateScore... intermediateScores) {
        final String configPrefix = "monteCarlo.heavy.";
        final Double expConstant = constants.getDouble(configPrefix+"explorationConstant", 3.0);
        final NodeUCTComparator comparator = new NodeUCTComparator(expConstant);
        final HeavyNode rootNode = new HeavyNode(new MiniBoard(board), null, comparator);
        return new TreeSimulation<>(constants, configPrefix, rootNode, coreNumber, intermediateScores);
    }
    public static TreeSimulation<LightNode> newLightTree(Board board,
                                                         Toml constants,
                                                         Integer coreNumber) {
        final String configPrefix = "monteCarlo.light.";
        final Double expConstant = constants.getDouble(configPrefix+"explorationConstant", 3.0);
        final NodeUCTComparator comparator = new NodeUCTComparator(expConstant);
        final LightNode rootNode = new LightNode(new MiniBoard(board), null, comparator);
        return new TreeSimulation<>(constants, configPrefix, rootNode, coreNumber);
    }
}