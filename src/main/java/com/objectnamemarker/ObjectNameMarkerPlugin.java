package com.objectnamemarker;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
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
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Object Name Markers",
		description = "Highlight objects by configured object names",
		tags = {"object", "marker", "highlight", "name", "tile"}
)
public class ObjectNameMarkerPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "objectnamemarker";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ObjectNameMarkerOverlay overlay;

	@Inject
	private ObjectNameMarkerConfig config;

	private final Set<TileObject> objects = new HashSet<>();

	private Set<String> hullObjectNames = new HashSet<>();
	private Set<String> outlineObjectNames = new HashSet<>();
	private Map<String, ObjectNameMarker> tileMarkers = new HashMap<>();

	@Provides
	ObjectNameMarkerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ObjectNameMarkerConfig.class);
	}

	@Override
	protected void startUp()
	{
		rebuildMarkers();
		overlayManager.add(overlay);
		rebuildObjectsOnClientThread();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		objects.clear();
		hullObjectNames.clear();
		outlineObjectNames.clear();
		tileMarkers.clear();
	}

	Set<TileObject> getObjects()
	{
		return objects;
	}

	Set<String> getHullObjectNames()
	{
		return hullObjectNames;
	}

	Set<String> getOutlineObjectNames()
	{
		return outlineObjectNames;
	}

	Map<String, ObjectNameMarker> getTileMarkers()
	{
		return tileMarkers;
	}

	String getObjectName(TileObject object)
	{
		if (object == null)
		{
			return "";
		}

		ObjectComposition composition = client.getObjectDefinition(object.getId());

		if (composition == null)
		{
			return "";
		}

		try
		{
			ObjectComposition impostor = composition.getImpostor();

			if (impostor != null)
			{
				composition = impostor;
			}
		}
		catch (RuntimeException ignored)
		{
			// Some objects do not have a valid impostor/transform state.
		}

		String name = composition.getName();

		if (name == null || name.isEmpty() || "null".equalsIgnoreCase(name))
		{
			return "";
		}

		return ObjectNameMarkerParser.normalizeName(name);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		rebuildMarkers();
		rebuildObjectsOnClientThread();
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
			rebuildObjectsOnClientThread();
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
		hullObjectNames = ObjectNameMarkerParser.parseNames(config.hullObjectNames());
		outlineObjectNames = ObjectNameMarkerParser.parseNames(config.outlineObjectNames());
		tileMarkers = ObjectNameMarkerParser.parseTileMarkers(config.tileObjectNames());
	}

	private void rebuildObjectsOnClientThread()
	{
		clientThread.invokeLater(this::rebuildObjects);
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
		String objectName = getObjectName(object);

		if (objectName.isEmpty())
		{
			return;
		}

		if (hullObjectNames.contains(objectName)
				|| outlineObjectNames.contains(objectName)
				|| tileMarkers.containsKey(objectName))
		{
			objects.add(object);
		}
	}
}