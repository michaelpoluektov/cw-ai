package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;
import uk.ac.bris.cs.scotlandyard.ui.ai.score.IntermediateScore;


/**
 * Class providing static methods initialising new instances of Monte Carlo Tree Simulation, with the corresponding root
 * node and constants. Separate from {@link TreeSimulation} to prevent unnecessary modification.
 * @see TreeSimulation
 */
public class TreeFactory {

    public static TreeSimulation newHeavyTree(Board board,
                                              Toml constants,
                                              Integer coreNumber,
                                              IntermediateScore... intermediateScores) {
        final String configPrefix = "monteCarlo.heavy.";
        final Double expConstant = constants.getDouble(configPrefix+"explorationConstant");
        final NodeUCTComparator comparator = new NodeUCTComparator(expConstant);
        final HeavyNode rootNode = new HeavyNode(new MiniBoard(board), null, comparator);
        return new TreeSimulation(constants, rootNode, coreNumber, intermediateScores);
    }
    
    public static TreeSimulation newLightTree(Board board,
                                              Toml constants,
                                              Integer coreNumber) {
        final String configPrefix = "monteCarlo.light.";
        final Double expConstant = constants.getDouble(configPrefix+"explorationConstant");
        final NodeUCTComparator comparator = new NodeUCTComparator(expConstant);
        final LightNode rootNode = new LightNode(new MiniBoard(board), null, comparator);
        return new TreeSimulation(constants, rootNode, coreNumber);
    }
}