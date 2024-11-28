package org.example.matcher;

import java.util.Set;

/**
 * Provides configuration settings for the application.
 */
public interface ConfigProvider {

    int getThreadCount();

    int getChunkSize();

    boolean isCaseInsensitive();

    String getFilePath();

    Set<String> getSearchTerms();
}
