package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class holds our implementation of the dijkstra algorithm which has been optimised for undirected graphs. The
 * optimisation uses the fact that every edge has the same weight of 1 assigned to it. Therefore it does not need to
 * look at paths that travel through more nodes than the current best path.
 * We return a list of lists of integers. If the {@link Dijkstra} algorithm is called with Mrx, we return a list containing a
 * single list of the shortest distances to the detectives. If we call {@link Dijkstra} with detectives, we return a list
 * of lists of integers, each sublist containing the shortest distance from a particular detective to all the possible
 * locations MrX could be at given his latest reveal round. This feature was intended to be used for the detectives Ai.
 */

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
