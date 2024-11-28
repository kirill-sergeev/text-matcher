package org.example.matcher.impl;

import org.example.matcher.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides configuration settings parsed from command-line arguments.
 * Example usage:
 * <pre>
 * java -jar matcher.jar --file <path> --search <terms> [OPTIONS]
 * </pre>
 */
public class CommandLineConfigProvider implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineConfigProvider.class);

    private final int threadCount;
    private final int chunkSize;
    private final boolean caseInsensitive;
    private final String filePath;
    private final Set<String> searchTerms;

    protected CommandLineConfigProvider(String filePath, Set<String> searchTerms, int threadCount, int chunkSize, boolean caseInsensitive) {
        this.threadCount = threadCount;
        this.chunkSize = chunkSize;
        this.caseInsensitive = caseInsensitive;
        this.filePath = filePath;
        this.searchTerms = Collections.unmodifiableSet(searchTerms);
    }

    public static CommandLineConfigProvider fromArgs(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            handleCommandLineError("");
        }

        int threadCount = Runtime.getRuntime().availableProcessors();
        int chunkSize = 1000;
        boolean caseInsensitive = false;
        String filePath = null;
        Set<String> searchTerms = new HashSet<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--threads":
                    threadCount = parseNumber("--threads", args[++i], 1, 100);
                    break;
                case "--chunk":
                    chunkSize = parseNumber("--chunk", args[++i], 1, 1000000);
                    break;
                case "--ignoreCase":
                    caseInsensitive = true;
                    break;
                case "--file":
                    filePath = args[++i];
                    break;
                case "--search":
                    searchTerms.addAll(Arrays.asList(args[++i].split(",")));
                    break;
                default:
                    handleCommandLineError("Unknown argument: " + args[i]);
                    break;
            }
        }

        validateConfig(filePath, searchTerms);
        logger.info("Loaded config from program arguments: " +
                        "filePath='{}', searchTerms={}, threadCount={}, chunkSize={}, caseInsensitive={}",
                filePath, searchTerms, threadCount, chunkSize, caseInsensitive);
        return new CommandLineConfigProvider(filePath, searchTerms, threadCount, chunkSize, caseInsensitive);
    }

    private static void handleCommandLineError(String errorMessage) {
        System.out.println(errorMessage);
        displayHelp();
        throw new IllegalArgumentException("Execution aborted. " + errorMessage);
    }

    private static void displayHelp() {
        System.out.println("Usage: java -jar matcher.jar --file <path> --search <terms> [OPTIONS]\n" +
                "Options:\n" +
                "--file <path>          * Path to the file to process (required).\n" +
                "--search <terms>       * Comma-separated list of search terms (required).\n" +
                "--threads <number>     Number of threads to use (default: number of available processors). Must be between 1 and 100.\n" +
                "--chunk <number>       Number of lines per chunk (default: 1000). Must be between 1 and 1,000,000.\n" +
                "--ignoreCase           Whether to perform a case-insensitive search (default: false).\n" +
                "--help                 Display this help message."
        );
    }

    private static void validateConfig(String filePath, Set<String> searchTerms) {
        if (filePath == null || filePath.isBlank()) {
            handleCommandLineError("'--file' is a required argument.");
        }
        if (searchTerms.isEmpty()) {
            handleCommandLineError("'--search' is a required argument.");
        }
    }

    private static int parseNumber(String paramName, String paramValue, int minValue, int maxValue) {
        try {
            int value = Integer.parseInt(paramValue);
            if (value < minValue || value > maxValue) {
                handleCommandLineError(String.format("Parameter '%s' should be in range [%d, %d]. Provided: %s",
                        paramName, minValue, maxValue, paramValue));
            }
            return value;
        } catch (NumberFormatException e) {
            handleCommandLineError(String.format("Incorrect number format for parameter '%s'. Provided: %s",
                    paramName, paramValue));
            return -1;
        }
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public Set<String> getSearchTerms() {
        return searchTerms;
    }
}