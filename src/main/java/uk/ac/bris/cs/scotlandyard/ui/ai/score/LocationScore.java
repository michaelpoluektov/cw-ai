package uk.ac.bris.cs.scotlandyard.ui.ai.score;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class LocationScore extends AbstractScore{
    private final ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    private final ImmutableList<Integer> sources;
    private final ImmutableList<Integer> destinations;
    private final Integer nodeSize;
    private final List<List<Short>> filteredDistanceToSourceList;
    private final double locationScoreExp = 2;
    public LocationScore(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph,
                         ImmutableList<Integer> sources,
                         ImmutableList<Integer> destinations) {
        this.graph = graph;
        this.sources = sources;
        this.destinations = destinations;
        this.nodeSize = graph.nodes().size();
        this.filteredDistanceToSourceList = filterDistanceToSourceList(combineSourcesDistances());
    }
    private List<List<Short>> filterDistanceToSourceList(List<List<Short>> distanceToSourceList){
        List<List<Short>> filteredList = new ArrayList<>();
        for(List<Short> shortList : distanceToSourceList) {
            List<Short> distances = new ArrayList<>();
            for(int i = 0; i < shortList.size(); i++) {
                if(destinations.contains(i+1)) distances.add(shortList.get(i));
            }
            filteredList.add(distances);
        }
        return filteredList;
    }

    private List<List<Short>> combineSourcesDistances() {
        Short [][] sourcesDistances = new Short[sources.size()][nodeSize];
        for(int i = 0; i < sources.size(); i++) {
            sourcesDistances[i] = DijkstraOfSource(sources.get(i));
        }
        return Arrays.stream(sourcesDistances)
                .map(Arrays::asList)
                .collect(Collectors.toList());
    }

    private Short[] DijkstraOfSource(Integer source) {
        Short[] distanceToSource = new Short[nodeSize];
        HashSet<Integer> unvisitedNodes = Sets.newHashSet();
        Short distanceOfClosestNodeToSource;
        Integer closestNodeToSource = source;
        for(int i = 1; i <= nodeSize; i++) unvisitedNodes.add(i);
        unvisitedNodes.remove(source);
        for(int i = 0; i < nodeSize; i++) distanceToSource[i] = Short.MAX_VALUE; // OUT OF BOUNDS!?
        distanceToSource[source - 1] = 0;
        for(Integer node : graph.adjacentNodes(source)) distanceToSource[node-1] = 1;
        while(!unvisitedNodes.isEmpty()) {
            distanceOfClosestNodeToSource = Short.MAX_VALUE;
            for(int i = 0; i < nodeSize; i++) {
                if(distanceToSource[i] < distanceOfClosestNodeToSource && unvisitedNodes.contains(i + 1)) {
                    closestNodeToSource = i + 1;
                    distanceOfClosestNodeToSource = distanceToSource[i];
                }
            }
            unvisitedNodes.remove(closestNodeToSource);
            for(Integer node : graph.adjacentNodes(closestNodeToSource)) {
                if(distanceOfClosestNodeToSource + 1 < distanceToSource[node -1]) {
                    distanceToSource[node - 1] = (short) (distanceOfClosestNodeToSource + 1);
                }
            }
        }
        return distanceToSource;
    }

    @Nonnull @Override public Double getMrXScore() {
        var detectiveDistancesToMrX = filteredDistanceToSourceList.get(0);
        Collections.sort(detectiveDistancesToMrX);
        List<Double> weightsList = detectiveDistancesToMrX.stream()
                .map(value -> pow(locationScoreExp, -value)/(1-pow(locationScoreExp,-1)))
                .collect(Collectors.toList());
        Double weightsListSum = weightsList.stream().reduce(0.0,Double::sum);
        weightsList = weightsList.stream().map(value -> value/weightsListSum).collect(Collectors.toList());


        double totalScore = 0.0;
        for(int i = 0; i < detectiveDistancesToMrX.size(); i++) {
            Double unweightedScore = 1-pow(locationScoreExp, 1-detectiveDistancesToMrX.get(i));
            totalScore += weightsList.get(i)*unweightedScore;
        }
        return totalScore;
    }
    @Nonnull @Override public Double getDetectivesScore() {
        return 0.0;
    }
}
