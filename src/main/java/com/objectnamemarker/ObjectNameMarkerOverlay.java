package com.objectnamemarker;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.util.Comparator;

class ObjectNameMarkerOverlay extends Overlay
{
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_FEATHER = 0;

    private final Client client;
    private final ObjectNameMarkerPlugin plugin;
    private final ObjectNameMarkerConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private ObjectNameMarkerOverlay(
            Client client,
            ObjectNameMarkerPlugin plugin,
            ObjectNameMarkerConfig config,
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
        objects.sort(Comparator.comparingInt(object -> object.getWorldLocation().distanceTo(playerLocation)));
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

            String objectName = plugin.getObjectName(object);

            if (objectName.isEmpty())
            {
                continue;
            }

            boolean renderedSomething = false;

            if (plugin.getHullObjectNames().contains(objectName))
            {
                renderHull(graphics, object, stroke);
                renderedSomething = true;
            }

            if (plugin.getOutlineObjectNames().contains(objectName))
            {
                renderModelOutline(object);
                renderedSomething = true;
            }

            ObjectNameMarker tileMarker = plugin.getTileMarkers().get(objectName);
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

        OverlayUtil.renderPolygon(graphics, hull, config.hullColor(), config.hullColor(), stroke);
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

        int objectMinX = base.getX();
        int objectMinY = base.getY();
        int objectMaxX = base.getX() + object.sizeX() - 1;
        int objectMaxY = base.getY() + object.sizeY() - 1;

        renderTileArea(
                graphics,
                objectMinX - radius,
                objectMinY - radius,
                objectMaxX + radius,
                objectMaxY + radius,
                objectMinX,
                objectMinY,
                objectMaxX,
                objectMaxY,
                object.sizeX(),
                object.sizeY(),
                object.getPlane(),
                stroke
        );
    }

    private void renderSingleTileObjectTiles(Graphics2D graphics, TileObject object, int radius, Stroke stroke)
    {
        WorldPoint base = object.getWorldLocation();

        renderTileArea(
                graphics,
                base.getX() - radius,
                base.getY() - radius,
                base.getX() + radius,
                base.getY() + radius,
                base.getX(),
                base.getY(),
                base.getX(),
                base.getY(),
                1,
                1,
                base.getPlane(),
                stroke
        );
    }

    private void renderTileArea(
            Graphics2D graphics,
            int minX,
            int minY,
            int maxX,
            int maxY,
            int objectMinX,
            int objectMinY,
            int objectMaxX,
            int objectMaxY,
            int objectWidth,
            int objectHeight,
            int plane,
            Stroke stroke
    )
    {
        CoreRect core = getCoreRect(objectMinX, objectMinY, objectMaxX, objectMaxY);
        boolean canFade = objectWidth >= 3 && objectHeight >= 3;
        int maxDistance = getMaxDistanceFromCore(minX, minY, maxX, maxY, core);

        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                LocalPoint localPoint = LocalPoint.fromWorld(client, new WorldPoint(x, y, plane));

                if (localPoint == null)
                {
                    continue;
                }

                Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

                if (tilePoly == null)
                {
                    continue;
                }

                int distanceFromCore = distanceFromRect(x, y, core.minX, core.minY, core.maxX, core.maxY);
                int distanceFromObject = distanceFromRect(x, y, objectMinX, objectMinY, objectMaxX, objectMaxY);

                Color fillColor;
                if (canFade || distanceFromObject > 0)
                {
                    fillColor = getTileFillColor(distanceFromCore, maxDistance);
                }
                else
                {
                    fillColor = config.tileFillColor();
                }

                OverlayUtil.renderPolygon(
                        graphics,
                        tilePoly,
                        config.tileBorderColor(),
                        fillColor,
                        stroke
                );
            }
        }
    }

    private Color getTileFillColor(int distanceFromCore, int maxDistance)
    {
        Color base = config.tileFillColor();

        if (!config.fadeTileOpacity() || maxDistance <= 0 || distanceFromCore <= 0)
        {
            return base;
        }

        int effectiveMaxDistance = Math.max(maxDistance, 3);

        double t = Math.min(1.0, distanceFromCore / (double) effectiveMaxDistance);
        double alphaMultiplier = 1.0 - (0.5 * t);
        int alpha = (int) Math.round(base.getAlpha() * alphaMultiplier);

        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private int getMaxDistanceFromCore(int minX, int minY, int maxX, int maxY, CoreRect core)
    {
        int maxDistance = distanceFromRect(minX, minY, core.minX, core.minY, core.maxX, core.maxY);
        maxDistance = Math.max(maxDistance, distanceFromRect(maxX, minY, core.minX, core.minY, core.maxX, core.maxY));
        maxDistance = Math.max(maxDistance, distanceFromRect(minX, maxY, core.minX, core.minY, core.maxX, core.maxY));
        maxDistance = Math.max(maxDistance, distanceFromRect(maxX, maxY, core.minX, core.minY, core.maxX, core.maxY));
        return maxDistance;
    }

    private CoreRect getCoreRect(int minX, int minY, int maxX, int maxY)
    {
        return new CoreRect(
                (minX + maxX) / 2,
                (minY + maxY) / 2,
                (minX + maxX + 1) / 2,
                (minY + maxY + 1) / 2
        );
    }

    private int distanceFromRect(int x, int y, int minX, int minY, int maxX, int maxY)
    {
        int dx = 0;

        if (x < minX)
        {
            dx = minX - x;
        }
        else if (x > maxX)
        {
            dx = x - maxX;
        }

        int dy = 0;

        if (y < minY)
        {
            dy = minY - y;
        }
        else if (y > maxY)
        {
            dy = y - maxY;
        }

        return Math.max(dx, dy);
    }

    private static class CoreRect
    {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private CoreRect(int minX, int minY, int maxX, int maxY)
        {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }
}