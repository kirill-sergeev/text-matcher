package org.example.matcher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Interface for aggregating results from multiple partial processing tasks.
 * It collects and combines partial results into a final comprehensive result.
 */
public interface ResultAggregator {

    void aggregateResults(Future<Map<String, List<Location>>> partialResults);

    Map<String, List<Location>> computeFinalResult();
}