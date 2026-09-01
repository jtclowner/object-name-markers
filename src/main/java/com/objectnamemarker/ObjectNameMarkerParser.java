package com.objectnamemarker;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

class ObjectNameMarkerParser
{
    private static final Pattern LEGACY_TILE_SUFFIX = Pattern.compile("[1-9]\\d*");

    static Set<String> parseNames(String input)
    {
        Set<String> names = new HashSet<>();

        for (String token : tokens(input))
        {
            String name = normalizeName(token);

            if (!name.isEmpty())
            {
                names.add(name);
            }
        }

        return names;
    }

    static Set<String> parseTileNames(String input)
    {
        Set<String> names = new HashSet<>();

        for (String token : tokens(input))
        {
            String name = token;
            int separator = token.lastIndexOf(":");

            // Keep legacy name:<positive number> configs working after tile expansion was removed.
            // The number is accepted for compatibility only and never affects rendering.
            if (separator >= 0
                    && LEGACY_TILE_SUFFIX.matcher(token.substring(separator + 1).trim()).matches())
            {
                name = token.substring(0, separator);
            }

            String normalizedName = normalizeName(name);

            if (!normalizedName.isEmpty())
            {
                names.add(normalizedName);
            }
        }

        return names;
    }

    static String normalizeName(String name)
    {
        if (name == null)
        {
            return "";
        }

        return name.trim().toLowerCase();
    }

    private static Set<String> tokens(String input)
    {
        Set<String> tokens = new HashSet<>();

        if (input == null || input.trim().isEmpty())
        {
            return tokens;
        }

        for (String rawLine : input.split("\\R"))
        {
            String line = rawLine.trim();

            int commentIndex = line.indexOf("#");
            if (commentIndex >= 0)
            {
                line = line.substring(0, commentIndex).trim();
            }

            if (line.isEmpty())
            {
                continue;
            }

            for (String token : line.split(","))
            {
                String trimmed = token.trim();

                if (!trimmed.isEmpty())
                {
                    tokens.add(trimmed);
                }
            }
        }

        return tokens;
    }
}
