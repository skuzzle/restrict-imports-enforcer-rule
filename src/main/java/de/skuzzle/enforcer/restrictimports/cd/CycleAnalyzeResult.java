package de.skuzzle.enforcer.restrictimports.cd;

public final class CycleAnalyzeResult {

    private final boolean hasCycle;

    public CycleAnalyzeResult(boolean hasCycle) {
        this.hasCycle = hasCycle;
    }
}
