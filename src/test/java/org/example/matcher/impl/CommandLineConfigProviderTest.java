package org.example.matcher.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CommandLineConfigProviderTest {

    @Test
    @DisplayName("fromArgs should parse valid arguments successfully")
    void fromArgs_shouldParseValidArguments() {
        String[] args = {
                "--file", "test.txt",
                "--search", "term1,term2",
                "--threads", "4",
                "--chunk", "500",
                "--ignoreCase"
        };

        CommandLineConfigProvider config = CommandLineConfigProvider.fromArgs(args);

        assertEquals("test.txt", config.getFilePath());
        assertEquals(Set.of("term1", "term2"), config.getSearchTerms());
        assertEquals(4, config.getThreadCount());
        assertEquals(500, config.getChunkSize());
        assertTrue(config.isCaseInsensitive());
    }

    @Test
    @DisplayName("fromArgs should use default values for optional arguments")
    void fromArgs_shouldUseDefaultsForOptionalArguments() {
        String[] args = {
                "--file", "test.txt",
                "--search", "term1,term2"
        };

        CommandLineConfigProvider config = CommandLineConfigProvider.fromArgs(args);

        assertEquals(Runtime.getRuntime().availableProcessors(), config.getThreadCount());
        assertEquals(1000, config.getChunkSize());
        assertFalse(config.isCaseInsensitive());
    }

    @Test
    @DisplayName("fromArgs should throw exception for missing required arguments")
    void fromArgs_shouldThrowOnMissingRequiredArgument() {
        String[] args = {"--search", "term1,term2"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommandLineConfigProvider.fromArgs(args));

        assertTrue(exception.getMessage().contains("--file"));
    }

    @Test
    @DisplayName("fromArgs should throw exception for invalid thread count")
    void fromArgs_shouldThrowOnInvalidThreadCount() {
        String[] args = {
                "--file", "test.txt",
                "--search", "term1",
                "--threads", "invalid"
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommandLineConfigProvider.fromArgs(args));

        assertTrue(exception.getMessage().contains("--threads"));
    }

    @Test
    @DisplayName("fromArgs should throw exception for invalid chunk size")
    void fromArgs_shouldThrowOnInvalidChunkSize() {
        String[] args = {
                "--file", "test.txt",
                "--search", "term1",
                "--chunk", "1000001"
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommandLineConfigProvider.fromArgs(args));

        assertTrue(exception.getMessage().contains("--chunk"));
    }

    @Test
    @DisplayName("fromArgs should throw exception for unknown arguments")
    void fromArgs_shouldThrowOnUnknownArguments() {
        String[] args = {
                "--file", "test.txt",
                "--search", "term1,term2",
                "--unknown", "value"
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommandLineConfigProvider.fromArgs(args));

        assertTrue(exception.getMessage().contains("Unknown argument"));
    }

    @Test
    @DisplayName("fromArgs should abort execution on help command")
    void fromArgs_shouldThrowOnHelp() {
        String[] args = {"--help"};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CommandLineConfigProvider.fromArgs(args));

        assertTrue(exception.getMessage().contains("Execution aborted."));
    }
}
