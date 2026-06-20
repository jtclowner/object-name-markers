package com.objectidmarker;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Object ID Marker",
		description = "Highlight objects by configured object IDs",
		tags = {"object", "marker", "highlight", "id", "tile"}
)
public class ObjectIdMarkerPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "objectidmarker";

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ObjectIdMarkerOverlay overlay;

	@Inject
	private ObjectIdMarkerConfig config;

	private final Set<TileObject> objects = new HashSet<>();

	private Set<Integer> hullObjectIds = new HashSet<>();
	private Set<Integer> outlineObjectIds = new HashSet<>();
	private Map<Integer, ObjectIdMarker> tileMarkers = new HashMap<>();

	@Provides
	ObjectIdMarkerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ObjectIdMarkerConfig.class);
	}

	@Override
	protected void startUp()
	{
		rebuildMarkers();
		rebuildObjects();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		objects.clear();
		hullObjectIds.clear();
		outlineObjectIds.clear();
		tileMarkers.clear();
	}

	Set<TileObject> getObjects()
	{
		return objects;
	}

	Set<Integer> getHullObjectIds()
	{
		return hullObjectIds;
	}

	Set<Integer> getOutlineObjectIds()
	{
		return outlineObjectIds;
	}

	Map<Integer, ObjectIdMarker> getTileMarkers()
	{
		return tileMarkers;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		rebuildMarkers();
		rebuildObjects();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			objects.clear();
		}
		else if (event.getGameState() == GameState.LOGGED_IN)
		{
			rebuildObjects();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		checkObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		objects.remove(event.getGameObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		checkObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		objects.remove(event.getWallObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		checkObject(event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		objects.remove(event.getDecorativeObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		checkObject(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		objects.remove(event.getGroundObject());
	}

	private void rebuildMarkers()
	{
		hullObjectIds = ObjectIdMarkerParser.parseIds(config.hullObjectIds());
		outlineObjectIds = ObjectIdMarkerParser.parseIds(config.outlineObjectIds());
		tileMarkers = ObjectIdMarkerParser.parseTileMarkers(config.tileObjectIds());
	}

	private void rebuildObjects()
	{
		objects.clear();

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		for (int plane = 0; plane < tiles.length; plane++)
		{
			for (int x = 0; x < tiles[plane].length; x++)
			{
				for (int y = 0; y < tiles[plane][x].length; y++)
				{
					Tile tile = tiles[plane][x][y];

					if (tile == null)
					{
						continue;
					}

					for (GameObject gameObject : tile.getGameObjects())
					{
						checkObject(gameObject);
					}

					checkObject(tile.getWallObject());
					checkObject(tile.getDecorativeObject());
					checkObject(tile.getGroundObject());
				}
			}
		}
	}

	private void checkObject(TileObject object)
	{
		if (object == null)
		{
			return;
		}

		int objectId = object.getId();

		if (hullObjectIds.contains(objectId)
				|| outlineObjectIds.contains(objectId)
				|| tileMarkers.containsKey(objectId))
		{
			objects.add(object);
		}
	}
}