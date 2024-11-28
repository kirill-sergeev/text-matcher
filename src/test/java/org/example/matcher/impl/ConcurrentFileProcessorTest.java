package org.example.matcher.impl;

import org.example.matcher.FileProcessor;
import org.example.matcher.ResultAggregator;
import org.example.matcher.TextMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConcurrentFileProcessorTest {

    TextMatcher mockMatcher = Mockito.mock(TextMatcher.class);
    ResultAggregator mockResultAggregator = mock(ResultAggregator.class);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    FileProcessor fileProcessor = new ConcurrentFileProcessor(mockMatcher, executorService, 2);

    @Test
    @DisplayName("constructor should throw IllegalArgumentException for invalid chunk size")
    void constructor_shouldThrowExceptionForInvalidChunkSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrentFileProcessor(mockMatcher, executorService, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrentFileProcessor(mockMatcher, executorService, -1));
    }

    @Test
    @DisplayName("constructor should throw IllegalArgumentException for null matcher")
    void constructor_shouldThrowExceptionForNullMatcher() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrentFileProcessor(null, executorService, 2));
    }

    @Test
    @DisplayName("constructor should throw IllegalArgumentException for null or shutdown executor service")
    void constructor_shouldThrowExceptionForInvalidExecutorService() {
        executorService.shutdown();

        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrentFileProcessor(mockMatcher, null, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrentFileProcessor(mockMatcher, executorService, 2));
    }

    @Test
    @DisplayName("processTextFile should throw IllegalArgumentException for non-existing file")
    void processTextFile_shouldThrowExceptionForNonExistingFile() {
        assertThrows(IllegalArgumentException.class,
                () -> fileProcessor.processTextFile("nonexistent.txt", mockResultAggregator));
    }

    @Test
    @DisplayName("processTextFile should throw IllegalArgumentException for null result aggregator")
    void processTextFile_shouldThrowExceptionForNullResultAggregator() {
        assertThrows(IllegalArgumentException.class,
                () -> fileProcessor.processTextFile("validFile.txt", null));
    }

    @Test
    @DisplayName("processTextFile should submit chunks to result aggregator")
    void processTextFile_shouldSubmitChunksToResultAggregator() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, List.of("line1", "line2", "line3", "line4"));

        fileProcessor.processTextFile(tempFile.toString(), mockResultAggregator);

        verify(mockResultAggregator, times(2)).aggregateResults(any(Future.class));
    }
}
