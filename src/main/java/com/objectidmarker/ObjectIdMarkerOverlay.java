package com.objectidmarker;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

class ObjectIdMarkerOverlay extends Overlay
{
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_FEATHER = 0;

    private final Client client;
    private final ObjectIdMarkerPlugin plugin;
    private final ObjectIdMarkerConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private ObjectIdMarkerOverlay(
            Client client,
            ObjectIdMarkerPlugin plugin,
            ObjectIdMarkerConfig config,
            ModelOutlineRenderer modelOutlineRenderer
    )
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_LOW);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Player localPlayer = client.getLocalPlayer();

        if (localPlayer == null)
        {
            return null;
        }

        WorldPoint playerLocation = localPlayer.getWorldLocation();
        Stroke stroke = new BasicStroke(1);
        List<TileObject> objects = new ArrayList<>(plugin.getObjects());
        int rendered = 0;

        for (TileObject object : objects)
        {
            if (rendered >= config.maxHighlightedObjects())
            {
                break;
            }

            if (object.getWorldLocation().distanceTo(playerLocation) > config.renderDistance())
            {
                continue;
            }

            int objectId = object.getId();
            boolean renderedSomething = false;

            if (plugin.getHullObjectIds().contains(objectId))
            {
                renderHull(graphics, object, stroke);
                renderedSomething = true;
            }

            if (plugin.getOutlineObjectIds().contains(objectId))
            {
                renderModelOutline(object);
                renderedSomething = true;
            }

            ObjectIdMarker tileMarker = plugin.getTileMarkers().get(objectId);
            if (tileMarker != null)
            {
                int radius = tileMarker.getRadius() == null ? 0 : tileMarker.getRadius();
                renderTiles(graphics, object, radius, stroke);
                renderedSomething = true;
            }

            if (renderedSomething)
            {
                rendered++;
            }
        }

        return null;
    }

    private void renderModelOutline(TileObject object)
    {
        if (object instanceof GameObject)
        {
            modelOutlineRenderer.drawOutline((GameObject) object, OUTLINE_WIDTH, config.outlineColor(), OUTLINE_FEATHER);
            return;
        }

        if (object instanceof WallObject)
        {
            modelOutlineRenderer.drawOutline((WallObject) object, OUTLINE_WIDTH, config.outlineColor(), OUTLINE_FEATHER);
            return;
        }

        if (object instanceof DecorativeObject)
        {
            modelOutlineRenderer.drawOutline((DecorativeObject) object, OUTLINE_WIDTH, config.outlineColor(), OUTLINE_FEATHER);
            return;
        }

        if (object instanceof GroundObject)
        {
            modelOutlineRenderer.drawOutline((GroundObject) object, OUTLINE_WIDTH, config.outlineColor(), OUTLINE_FEATHER);
        }
    }

    private void renderHull(Graphics2D graphics, TileObject object, Stroke stroke)
    {
        Shape hull = getConvexHull(object);

        if (hull == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(
                graphics,
                hull,
                config.hullColor(),
                config.hullColor(),
                stroke
        );
    }

    private Shape getConvexHull(TileObject object)
    {
        if (object instanceof GameObject)
        {
            return ((GameObject) object).getConvexHull();
        }

        if (object instanceof WallObject)
        {
            return ((WallObject) object).getConvexHull();
        }

        if (object instanceof DecorativeObject)
        {
            return ((DecorativeObject) object).getConvexHull();
        }

        if (object instanceof GroundObject)
        {
            return ((GroundObject) object).getConvexHull();
        }

        return null;
    }

    private void renderTiles(Graphics2D graphics, TileObject object, int radius, Stroke stroke)
    {
        if (object instanceof GameObject)
        {
            renderGameObjectTiles(graphics, (GameObject) object, radius, stroke);
            return;
        }

        renderSingleTileObjectTiles(graphics, object, radius, stroke);
    }

    private void renderGameObjectTiles(Graphics2D graphics, GameObject object, int radius, Stroke stroke)
    {
        WorldPoint base = WorldPoint.fromScene(
                client,
                object.getSceneMinLocation().getX(),
                object.getSceneMinLocation().getY(),
                object.getPlane()
        );

        int minX = base.getX() - radius;
        int minY = base.getY() - radius;
        int maxX = base.getX() + object.sizeX() - 1 + radius;
        int maxY = base.getY() + object.sizeY() - 1 + radius;

        renderTileArea(graphics, minX, minY, maxX, maxY, object.getPlane(), stroke);
    }

    private void renderSingleTileObjectTiles(Graphics2D graphics, TileObject object, int radius, Stroke stroke)
    {
        WorldPoint base = object.getWorldLocation();

        int minX = base.getX() - radius;
        int minY = base.getY() - radius;
        int maxX = base.getX() + radius;
        int maxY = base.getY() + radius;

        renderTileArea(graphics, minX, minY, maxX, maxY, base.getPlane(), stroke);
    }

    private void renderTileArea(Graphics2D graphics, int minX, int minY, int maxX, int maxY, int plane, Stroke stroke)
    {
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                WorldPoint worldPoint = new WorldPoint(x, y, plane);
                LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

                if (localPoint == null)
                {
                    continue;
                }

                Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

                if (tilePoly != null)
                {
                    OverlayUtil.renderPolygon(
                            graphics,
                            tilePoly,
                            config.tileBorderColor(),
                            config.tileFillColor(),
                            stroke
                    );
                }
            }
        }
    }
}