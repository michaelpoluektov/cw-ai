package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.moandjiezana.toml.Toml;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Dijkstra {
    private final ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    private final Integer nodeSize;
    public Dijkstra(Board board) {
        this.graph = board.getSetup().graph;
        this.nodeSize = graph.nodes().size();
    }

    protected List<List<Integer>> getDistances(ImmutableList<Integer> sources, ImmutableList<Integer> destinations) {
        List<List<Integer>> distances = new ArrayList<>(sources.size());
        for(Integer source : sources) {
            List<Integer> distanceToSource = new ArrayList<>(Collections.nCopies(nodeSize, Integer.MAX_VALUE));
            Set<Integer> unvisitedNodes = IntStream.rangeClosed(1, nodeSize).boxed().collect(Collectors.toSet());
            Set<Integer> noDistanceDestinations = new HashSet<>(destinations);
            distanceToSource.set(source-1, 0);
            Integer closestNodeToSource = source;
            while(!noDistanceDestinations.isEmpty()) { // check this
                Integer distanceOfClosestNodeToSource = Integer.MAX_VALUE;
                for(int i = 0; i < nodeSize; i++) {
                    if(distanceToSource.get(i) < distanceOfClosestNodeToSource && unvisitedNodes.contains(i + 1)) {
                        closestNodeToSource = i + 1;
                        distanceOfClosestNodeToSource = distanceToSource.get(i);
                    }
                }
                unvisitedNodes.remove(closestNodeToSource);
                for(Integer node : graph.adjacentNodes(closestNodeToSource)) {
                    if(distanceOfClosestNodeToSource + 1 < distanceToSource.get(node - 1)) {
                        distanceToSource.set(node - 1, distanceOfClosestNodeToSource + 1);
                        noDistanceDestinations.remove(node);
                    }
                }
            }
            distances.add(destinations.stream()
                    .map(destination -> distanceToSource.get(destination - 1))
                    .collect(Collectors.toList()));
        }
        return distances;
    }
}
