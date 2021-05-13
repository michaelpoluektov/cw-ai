package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo;

import java.util.Comparator;

public class NodeUCTComparator implements Comparator<AbstractNode> {
    private final Double explorationConstant;
    public NodeUCTComparator(Double explorationConstant) {
        this.explorationConstant = explorationConstant;
    }

    private Double getUCTScore(AbstractNode node){
        if(node.getParent().isEmpty()) throw new IllegalArgumentException("Root node doesn't have a parent");
        Double exploitation = node.getAverageScore();
        Double exploration = explorationConstant * Math.sqrt(Math.log(node.getParent().get().getPlays())/node.getPlays());
        return exploitation + exploration;
    }
    @Override
    public int compare(AbstractNode o1, AbstractNode o2) {
        return Double.compare(getUCTScore(o1), getUCTScore(o2));
    }
}