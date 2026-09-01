package com.objectnamemarker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectNameMarkerParserTest
{
    @Test
    public void stripsLegacyPositiveNumericSuffixesFromTileNames()
    {
        assertEquals(
                Collections.singleton("bank booth"),
                ObjectNameMarkerParser.parseTileNames(
                        "Bank booth:1, Bank booth:2, Bank booth:3, Bank booth:999999999999999999999"
                )
        );
    }

    @Test
    public void leavesNonPositiveAndNonNumericSuffixesAsPartOfTheName()
    {
        assertEquals(
                new HashSet<>(Arrays.asList("bank booth:0", "bank booth:-1", "bank booth:custom")),
                ObjectNameMarkerParser.parseTileNames("Bank booth:0, Bank booth:-1, Bank booth:custom")
        );
    }
}
