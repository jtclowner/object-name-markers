package com.objectnamemarker;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("objectnamemarker")
public interface ObjectNameMarkerConfig extends Config
{
	@ConfigItem(
			keyName = "hullObjectNames",
			name = "Object hull highlight",
			description = "Object names to render as filled convex hulls. Supports commas, new lines, and # comments.",
			position = 0
	)
	default String hullObjectNames()
	{
		return "";
	}

	@ConfigItem(
			keyName = "outlineObjectNames",
			name = "Object outline highlight",
			description = "Object names to render with model outlines. Supports commas, new lines, and # comments.",
			position = 1
	)
	default String outlineObjectNames()
	{
		return "";
	}

	@ConfigItem(
			keyName = "tileObjectNames",
			name = "Object tile highlight",
			description = "Object names to render tile highlights for. Format: name or name:radius. Radius must be positive and expands outward.",
			position = 2
	)
	default String tileObjectNames()
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
		return new Color(0, 255, 0, 128);
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
		return new Color(0, 255, 0, 128);
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

	@ConfigItem(
			keyName = "fadeTileOpacity",
			name = "Fade tile opacity",
			description = "Fade tile fill opacity from the centre/core outward.",
			position = 7
	)
	default boolean fadeTileOpacity()
	{
		return false;
	}

	@Range(min = 1, max = 1000)
	@ConfigItem(
			keyName = "maxHighlightedObjects",
			name = "Max highlighted objects",
			description = "Safety limit for the number of matching objects rendered at once.",
			position = 8
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
			position = 9
	)
	default int renderDistance()
	{
		return 32;
	}
}