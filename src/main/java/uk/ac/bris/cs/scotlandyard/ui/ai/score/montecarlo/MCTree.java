package uk.ac.bris.cs.scotlandyard.ui.ai.score.montecarlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

public class MCTree {
    MCNode rootNode;
    public MCTree(Board board, Toml constants){
        rootNode = new MCNode(board, constants);
    }
}
