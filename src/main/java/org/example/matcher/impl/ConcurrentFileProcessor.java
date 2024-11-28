package org.example.matcher.impl;

import org.example.matcher.FileProcessor;
import org.example.matcher.ResultAggregator;
import org.example.matcher.TextMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Processes a text file concurrently by splitting it into chunks and passing each chunk to a {@link TextMatcher}.
 * Results from all chunks are aggregated using a {@link ResultAggregator}.
 */
public class ConcurrentFileProcessor implements FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentFileProcessor.class);

    private final int chunkSize;
    private final ExecutorService executorService;
    private final TextMatcher matcher;

    public ConcurrentFileProcessor(TextMatcher matcher, ExecutorService executorService, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive.");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("Matcher must not be null.");
        }
        if (executorService == null || executorService.isShutdown()) {
            throw new IllegalArgumentException("ExecutorService must not be null or in shut down state.");
        }

        this.matcher = matcher;
        this.executorService = executorService;
        this.chunkSize = chunkSize;
    }

    @Override
    public void processTextFile(String filePath, ResultAggregator resultAggregator) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("The specified file does not exist: " + filePath);
        }
        if (resultAggregator == null) {
            throw new IllegalArgumentException("ResultAggregator must not be null.");
        }

        logger.info("Processing file: {}", filePath);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> lines = new ArrayList<>();
            int lineOffset = 0;
            long charOffset = 0;
            long currentChunkLength = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                currentChunkLength += line.length() + System.lineSeparator().length();

                if (lines.size() == chunkSize) {
                    submitChunk(resultAggregator, lines, lineOffset, charOffset);
                    lineOffset += lines.size();
                    charOffset += currentChunkLength;
                    currentChunkLength = 0;
                    lines = new ArrayList<>();
                }
            }

            if (!lines.isEmpty()) {
                submitChunk(resultAggregator, lines, lineOffset, charOffset);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("File processing failed: " + filePath, e);
        }
    }

    private void submitChunk(ResultAggregator resultAggregator, List<String> lines, int lineOffset, long charOffset) {
        resultAggregator.aggregateResults(
                executorService.submit(() -> matcher.findMatches(lines, lineOffset, charOffset)));
    }
}
