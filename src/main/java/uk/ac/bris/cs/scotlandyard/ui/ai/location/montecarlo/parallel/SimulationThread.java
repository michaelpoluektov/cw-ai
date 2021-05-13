package uk.ac.bris.cs.scotlandyard.ui.ai.location.montecarlo.parallel;

public class SimulationThread implements Runnable {
    ParallelRootNode rootNode;
    public SimulationThread(ParallelRootNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public void run() {
        rootNode.runSingleSimulation();
    }
}
