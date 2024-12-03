package org.example.matcher.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A text matcher that uses regular expressions to find occurrences
 * of search terms within a given input text.
 */
public class RegexTextMatcher extends AbstractTextMatcher {

    private final Pattern combinedPattern;

    public RegexTextMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        super(searchTerms, caseInsensitive);

        String patternString = this.orderedSearchTerms.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        this.combinedPattern = caseInsensitive
                ? Pattern.compile(patternString, Pattern.CASE_INSENSITIVE)
                : Pattern.compile(patternString);
    }

    @Override
    protected Map<String, List<Integer>> computeMatches(String lines) {
        Map<String, List<Integer>> matches = new HashMap<>();
        Matcher matcher = combinedPattern.matcher(lines);
        while (matcher.find()) {
            int matchStart = matcher.start();
            String matchedPattern = matcher.group();
            matches.computeIfAbsent(matchedPattern, k -> new ArrayList<>()).add(matchStart);
        }
        return matches;
    }
}