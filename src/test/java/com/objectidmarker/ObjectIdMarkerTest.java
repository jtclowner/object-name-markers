package com.objectidmarker;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ObjectIdMarkerTest
{
	@Test
	public void testParseIds()
	{
		Set<Integer> ids = ObjectIdMarkerParser.parseIds(
				"1234, 5678\n" +
						"# comment\n" +
						"9999"
		);

		assertTrue(ids.contains(1234));
		assertTrue(ids.contains(5678));
		assertTrue(ids.contains(9999));
		assertEquals(3, ids.size());
	}

	@Test
	public void testParseTileMarkers()
	{
		Map<Integer, ObjectIdMarker> markers = ObjectIdMarkerParser.parseTileMarkers(
				"1234\n" +
						"5678:3\n" +
						"9999:0\n" +
						"1111:-1\n" +
						"# comment"
		);

		assertNull(markers.get(1234).getRadius());
		assertEquals(Integer.valueOf(3), markers.get(5678).getRadius());
		assertNull(markers.get(9999).getRadius());
		assertNull(markers.get(1111).getRadius());
	}
}