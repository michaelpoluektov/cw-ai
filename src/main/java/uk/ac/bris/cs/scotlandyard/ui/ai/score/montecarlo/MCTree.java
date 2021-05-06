package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;

public class MCTree {
    MCNode rootNode;
    public MCTree(Board board, Toml constants){
        rootNode = new MCNode(board, constants);
    }
}
