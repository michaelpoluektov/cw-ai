package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ImprovedDijkstra extends Dijkstra{
    private final ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    private final Integer nodeSize;
    public ImprovedDijkstra(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        super(graph);
        this.graph = graph;
        this.nodeSize = graph.nodes().size();
    }

    @Override public final List<List<Integer>> getDistances(ImmutableList<Integer> sources,
                                                  ImmutableList<Integer> destinations) {
        List<List<Integer>> distances = new ArrayList<>();
        for(Integer source : sources) {
            final List<Integer> distanceToSource = new ArrayList<>(Collections.nCopies(nodeSize, Integer.MAX_VALUE));
            distanceToSource.set(source - 1, 0);
            int currentDist = 0;
            final List<Integer> unreachedDestinations = new ArrayList<>(destinations);
            List<Integer> currentList = new LinkedList<>(Collections.singleton(source));
            List<Integer> nextList = new LinkedList<>();
            while(!unreachedDestinations.isEmpty()) {
                for(Integer node : currentList) {
                    for(Integer adjacentNode : graph.adjacentNodes(node)) {
                        if(distanceToSource.get(adjacentNode - 1) > currentDist + 1) {
                            distanceToSource.set(adjacentNode - 1, currentDist + 1);
                            nextList.add(adjacentNode);
                            unreachedDestinations.remove(adjacentNode);
                        }
                    }
                }
                currentList = nextList;
                nextList = new LinkedList<>();
                currentDist++;
            }
            distances.add(destinations.stream()
                    .map(destination -> distanceToSource.get(destination - 1))
                    .collect(Collectors.toList()));
        }
        return distances;
    }
}
