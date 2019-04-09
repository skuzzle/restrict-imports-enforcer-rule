package de.skuzzle.enforcer.restrictimports.cd;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;

class PackageCycleDetectorImpl implements PackageCycleDetector {

    @Override
    public CycleAnalyzeResult analyzeForCycles(Iterable<ParsedFile> parsedFiles) {
        final Graph<CycleParticipant> graph = buildGraph(parsedFiles);
        final boolean hasCycle = Graphs.hasCycle(graph);
        return new CycleAnalyzeResult(hasCycle);
    }

    private Graph<CycleParticipant> buildGraph(Iterable<ParsedFile> parsedFiles) {
        final MutableGraph<CycleParticipant> graph = GraphBuilder.directed()
                .allowsSelfLoops(false)
                .build();
        for (ParsedFile parsedFile : parsedFiles) {
            final CycleParticipant source = new CycleParticipant(parsedFile.getFqcn());
            parsedFile.getImports().forEach(imported -> {
                final CycleParticipant target = new CycleParticipant(imported.getFqcn());
                graph.putEdge(source, target);
            });
        }
        return graph;
    }
}
