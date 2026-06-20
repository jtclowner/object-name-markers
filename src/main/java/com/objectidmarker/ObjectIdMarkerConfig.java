package com.objectidmarker;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("objectidmarker")
public interface ObjectIdMarkerConfig extends Config
{
	@ConfigItem(
			keyName = "hullObjectIds",
			name = "Object hull highlight",
			description = "Object IDs to render as filled convex hulls. Supports commas, new lines, and # comments.",
			position = 0
	)
	default String hullObjectIds()
	{
		return "";
	}

	@ConfigItem(
			keyName = "outlineObjectIds",
			name = "Object outline highlight",
			description = "Object IDs to render with model outlines. Supports commas, new lines, and # comments.",
			position = 1
	)
	default String outlineObjectIds()
	{
		return "";
	}

	@ConfigItem(
			keyName = "tileObjectIds",
			name = "Object tile highlight",
			description = "Object IDs to render tile highlights for. Format: id or id:radius. Radius must be positive and expands outward.",
			position = 2
	)
	default String tileObjectIds()
	{
		return "";
	}

	@Alpha
	@ConfigItem(
			keyName = "hullColor",
			name = "Hull colour",
			description = "Fill colour for object hull highlights.",
			position = 3
	)
	default Color hullColor()
	{
		return new Color(0, 255, 0);
	}

	@Alpha
	@ConfigItem(
			keyName = "outlineColor",
			name = "Outline colour",
			description = "Colour for object outline highlights.",
			position = 4
	)
	default Color outlineColor()
	{
		return new Color(0, 255, 0);
	}

	@Alpha
	@ConfigItem(
			keyName = "tileFillColor",
			name = "Tile fill colour",
			description = "Fill colour for highlighted tiles.",
			position = 5
	)
	default Color tileFillColor()
	{
		return new Color(0, 255, 0, 60);
	}

	@Alpha
	@ConfigItem(
			keyName = "tileBorderColor",
			name = "Tile border colour",
			description = "Border colour for highlighted tiles.",
			position = 6
	)
	default Color tileBorderColor()
	{
		return new Color(0, 255, 0, 0);
	}

	@Range(min = 1, max = 1000)
	@ConfigItem(
			keyName = "maxHighlightedObjects",
			name = "Max highlighted objects",
			description = "Safety limit for the number of matching objects rendered at once.",
			position = 7
	)
	default int maxHighlightedObjects()
	{
		return 20;
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
			keyName = "renderDistance",
			name = "Render distance",
			description = "Only render matching objects within this many tiles.",
			position = 8
	)
	default int renderDistance()
	{
		return 32;
	}
}