package com.objectnamemarker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ObjectNameMarkerParser
{
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

    static Map<String, ObjectNameMarker> parseTileMarkers(String input)
    {
        Map<String, ObjectNameMarker> markers = new HashMap<>();

        for (String token : tokens(input))
        {
            String name = token;
            Integer radius = null;

            int radiusSeparator = token.lastIndexOf(":");
            if (radiusSeparator >= 0)
            {
                String possibleName = token.substring(0, radiusSeparator).trim();
                String possibleRadius = token.substring(radiusSeparator + 1).trim();

                try
                {
                    int parsedRadius = Integer.parseInt(possibleRadius);

                    if (parsedRadius > 0)
                    {
                        name = possibleName;
                        radius = parsedRadius;
                    }
                }
                catch (NumberFormatException ignored)
                {
                    // Treat the whole token as a name if the suffix is not a valid radius.
                }
            }

            String normalizedName = normalizeName(name);

            if (!normalizedName.isEmpty())
            {
                markers.put(normalizedName, new ObjectNameMarker(radius));
            }
        }

        return markers;
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