package org.example.matcher.impl;

import org.example.matcher.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasicResultAggregatorTest {

    Future<Map<String, List<Location>>> future = mock(Future.class);
    Future<Map<String, List<Location>>> future2 = mock(Future.class);
    BasicResultAggregator aggregator = new BasicResultAggregator();

    @Test
    @DisplayName("aggregateResults should aggregate results correctly when partial results are added")
    void aggregateResults_shouldProcessResultsCorrectly() throws Exception {

        Map<String, List<Location>> map1 = new HashMap<>();
        map1.put("key1", List.of(new Location(1, 2)));
        when(future.get()).thenReturn(map1);

        Map<String, List<Location>> map2 = new HashMap<>();
        map2.put("key1", List.of(new Location(3, 4)));
        map2.put("key2", List.of(new Location(5, 6)));
        when(future2.get()).thenReturn(map2);

        aggregator.aggregateResults(future);
        aggregator.aggregateResults(future2);
        Map<String, List<Location>> finalResult = aggregator.computeFinalResult();

        assertNotNull(finalResult);
        assertEquals(2, finalResult.size());
        assertTrue(finalResult.containsKey("key1"));
        assertTrue(finalResult.containsKey("key2"));
    }

    @Test
    @DisplayName("aggregateResults should throw IllegalStateException when trying to add results after final computation")
    void aggregateResults_shouldThrowAfterFinalComputation() {
        aggregator.computeFinalResult();

        assertThrows(IllegalStateException.class,
                () -> aggregator.aggregateResults(future));
    }

    @Test
    @DisplayName("computeFinalResult should handle InterruptedException during result aggregation")
    void computeFinalResult_shouldThrowOnInterruptedException() throws Exception {
        when(future.get()).thenThrow(new InterruptedException());

        aggregator.aggregateResults(future);

        assertThrows(IllegalStateException.class, aggregator::computeFinalResult);
    }

    @Test
    @DisplayName("computeFinalResult should handle ExecutionException during result aggregation")
    void computeFinalResult_shouldThrowOnExecutionException() throws Exception {
        when(future.get()).thenThrow(new ExecutionException(new RuntimeException("Test")));

        aggregator.aggregateResults(future);

        assertThrows(IllegalStateException.class, aggregator::computeFinalResult);
    }

    @Test
    @DisplayName("computeFinalResult should return an unmodifiable final result after computations")
    void computeFinalResult_shouldBeUnmodifiable() throws Exception {
        Map<String, List<Location>> map = new HashMap<>();
        map.put("key", List.of(new Location(1, 2)));
        when(future.get()).thenReturn(map);

        aggregator.aggregateResults(future);

        Map<String, List<Location>> finalResult = aggregator.computeFinalResult();
        assertThrows(UnsupportedOperationException.class,
                () -> finalResult.put("key", new ArrayList<>()));
    }

    @Test
    @DisplayName("computeFinalResult should not recalculate the result")
    void computeFinalResult_shouldNotRecalculateTheResult() throws Exception {
        Map<String, List<Location>> map = new HashMap<>();
        map.put("key", List.of(new Location(1, 2)));
        when(future.get()).thenReturn(map);

        aggregator.aggregateResults(future);

        aggregator.computeFinalResult();
        aggregator.computeFinalResult();

        verify(future, times(1)).get();
    }
}