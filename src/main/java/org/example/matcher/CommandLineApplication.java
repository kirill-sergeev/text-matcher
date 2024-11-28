package org.example.matcher;

import org.example.matcher.impl.AhoCorasickTextMatcher;
import org.example.matcher.impl.BasicResultAggregator;
import org.example.matcher.impl.CommandLineConfigProvider;
import org.example.matcher.impl.ConcurrentFileProcessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The entry point for the text matching CLI application.
 */
public class CommandLineApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigProvider configProvider = CommandLineConfigProvider.fromArgs(args);
        ExecutorService executorService = Executors.newFixedThreadPool(configProvider.getThreadCount());
        TextMatcher matcher = new AhoCorasickTextMatcher(configProvider.getSearchTerms(), configProvider.isCaseInsensitive());
        FileProcessor processor = new ConcurrentFileProcessor(matcher, executorService, configProvider.getChunkSize());

        ResultAggregator resultAggregator = new BasicResultAggregator();
        processor.processTextFile(configProvider.getFilePath(), resultAggregator);

        executorService.shutdown();
        if (executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            Map<String, List<Location>> result = resultAggregator.computeFinalResult();
            System.out.println("--------------------------------");
            if (result.isEmpty()) {
                System.out.println("No matches.");
            }
            result.forEach((k, v) -> System.out.printf("%-15s ---> %s%n", k, v));
        }
    }
}