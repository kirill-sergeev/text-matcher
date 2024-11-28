package org.example.matcher.impl;

import org.example.matcher.Location;
import org.example.matcher.ResultAggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A thread-safe implementation of the {@link ResultAggregator} interface.
 * This class collects partial results from multiple tasks and aggregates them into a final result.
 */
public class BasicResultAggregator implements ResultAggregator {

    private final List<Future<Map<String, List<Location>>>> futures = new ArrayList<>();
    private final Map<String, List<Location>> result = new HashMap<>();
    private boolean resultComputed = false;

    @Override
    public synchronized void aggregateResults(Future<Map<String, List<Location>>> partialResults) {
        if (resultComputed) {
            throw new IllegalStateException("Attempted to add results after final computation.");
        }
        futures.add(partialResults);
    }

    @Override
    public synchronized Map<String, List<Location>> computeFinalResult() {
        if (resultComputed) {
            return Collections.unmodifiableMap(result);
        }

        for (Future<Map<String, List<Location>>> future : futures) {
            try {
                Map<String, List<Location>> partialResult = future.get();
                for (Map.Entry<String, List<Location>> entry : partialResult.entrySet()) {
                    result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Result aggregation was interrupted.", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Error while processing a chunk.", e);
            }
        }
        resultComputed = true;
        return Collections.unmodifiableMap(result);
    }
}