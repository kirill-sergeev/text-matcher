package org.example.matcher.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple text matcher that performs naive substring search for a set of search terms
 * within a given input text.
 */
public class NaiveTextMatcher extends AbstractTextMatcher {

    public NaiveTextMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        super(searchTerms, caseInsensitive);
    }

    @Override
    protected Map<String, List<Integer>> computeMatches(String lines) {
        if (caseInsensitive) {
            lines = lines.toLowerCase();
        }

        Set<Integer> present = new HashSet<>();
        Map<String, List<Integer>> matches = new HashMap<>();
        for (String word : orderedSearchTerms) {
            int index = lines.indexOf(word);
            List<Integer> indexes = new ArrayList<>();
            while (index != -1) {
                if (present.add(index)) {
                    indexes.add(index);
                }
                index = lines.indexOf(word, index + 1);
            }
            if (!indexes.isEmpty()) {
                matches.put(word, indexes);
            }
        }
        return matches;
    }
}