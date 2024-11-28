package org.example.matcher;

import java.util.Objects;

/**
 * Represents a location within a text, including the line and character offsets.
 * The character offset is measured from the beginning of the file.
 */
public final class Location {

    final int lineOffset;
    final long charOffset;

    public Location(int lineOffset, long charOffset) {
        this.lineOffset = lineOffset;
        this.charOffset = charOffset;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    public long getCharOffset() {
        return charOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return lineOffset == location.lineOffset && charOffset == location.charOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineOffset, charOffset);
    }

    @Override
    public String toString() {
        return String.format("[lineOffset=%d, charOffset=%d]", lineOffset, charOffset);
    }
}