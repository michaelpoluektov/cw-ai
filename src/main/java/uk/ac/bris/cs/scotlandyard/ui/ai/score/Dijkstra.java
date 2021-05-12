package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.MiniBoard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Dijkstra {
    private final ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    public Dijkstra(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        this.graph = graph;
    }
    public final List<List<Integer>> getDistances(ImmutableList<Integer> sources,
                                               ImmutableList<Integer> destinations) {
        final int nodeSize = graph.nodes().size();
        final List<List<Integer>> distances = new ArrayList<>(sources.size());
        for(Integer source : sources) {
            final List<Integer> distanceToSource = new ArrayList<>(Collections.nCopies(nodeSize, Integer.MAX_VALUE));
            final Set<Integer> unvisitedNodes = IntStream.rangeClosed(1, nodeSize).boxed().collect(Collectors.toSet());
            final Set<Integer> noDistanceDestinations = new HashSet<>(destinations);
            distanceToSource.set(source-1, 0);
            Integer closestNodeToSource = source;
            while(!noDistanceDestinations.isEmpty()) {
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
