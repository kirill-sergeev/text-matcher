package org.example.matcher;

/**
 * Defines the contract for processing text files and aggregating results.
 */
public interface FileProcessor {

    /**
     * Processes the specified text file and aggregates the results using the given result aggregator.
     *
     * @param filePath         the path to the text file to be processed
     * @param resultAggregator the aggregator to collect and combine processing results
     */
    void processTextFile(String filePath, ResultAggregator resultAggregator);
}
