package com.objectidmarker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ObjectIdMarkerParser
{
    static Set<Integer> parseIds(String input)
    {
        Set<Integer> ids = new HashSet<>();

        for (String token : tokens(input))
        {
            try
            {
                ids.add(Integer.parseInt(token.trim()));
            }
            catch (NumberFormatException ignored)
            {
                // Ignore invalid tokens.
            }
        }

        return ids;
    }

    static Map<Integer, ObjectIdMarker> parseTileMarkers(String input)
    {
        Map<Integer, ObjectIdMarker> markers = new HashMap<>();

        for (String token : tokens(input))
        {
            String[] parts = token.split(":", 2);

            try
            {
                int objectId = Integer.parseInt(parts[0].trim());
                Integer radius = null;

                if (parts.length == 2)
                {
                    int parsedRadius = Integer.parseInt(parts[1].trim());

                    if (parsedRadius > 0)
                    {
                        radius = parsedRadius;
                    }
                }

                markers.put(objectId, new ObjectIdMarker(radius));
            }
            catch (NumberFormatException ignored)
            {
                // Ignore invalid tokens.
            }
        }

        return markers;
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