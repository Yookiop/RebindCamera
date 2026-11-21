package com.remapcamera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class CameraKeyOverlay extends Overlay
{
	/** The space the client keeps between the viewport and its top left overlays. */
	private static final int BORDER = 5;
	private static final int BORDER_TOP = BORDER + 15;

	private final Client client;
	private final RemapCameraPlugin plugin;
	private final RemapCameraConfig config;

	@Inject
	CameraKeyOverlay(Client client, RemapCameraPlugin plugin, RemapCameraConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.cameraKeysEnabled() || !config.keyIndicatorEnabled() || !plugin.cameraKeysHeld())
		{
			return null;
		}

		String text = config.indicatorText();

		if (text.isEmpty())
		{
			return null;
		}

		Point topLeft = topLeft();
		int x = topLeft.x + config.indicatorX();
		int y = topLeft.y + config.indicatorY() + chatboxOffset();

		graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, config.indicatorSize()));
		FontMetrics metrics = graphics.getFontMetrics();
		int baseline = y + metrics.getAscent();

		graphics.setColor(Color.BLACK);

		for (int offsetX = -1; offsetX <= 1; offsetX++)
		{
			for (int offsetY = -1; offsetY <= 1; offsetY++)
			{
				if (offsetX != 0 || offsetY != 0)
				{
					graphics.drawString(text, x + offsetX, baseline + offsetY);
				}
			}
		}

		graphics.setColor(config.indicatorColor());
		graphics.drawString(text, x, baseline);

		return new Dimension(x + metrics.stringWidth(text) + 2, y + metrics.getHeight());
	}

	private int chatboxOffset()
	{
		if (!config.indicatorRespectChatbox())
		{
			return 0;
		}

		Widget chatbox = client.getWidget(InterfaceID.Chatbox.CHATAREA);

		return chatbox == null || chatbox.isHidden() ? 0 : config.indicatorChatboxY();
	}

	/**
	 * The place the client starts its stack of top left overlays, so the indicator keeps the spot it
	 * was configured for. It is not part of that stack itself: another top left overlay, such as the
	 * opponent info of a fight, would otherwise push it down until that overlay disappears again.
	 */
	private Point topLeft()
	{
		Widget viewport = viewportLayer();
		Rectangle bounds = viewport != null ? viewport.getBounds() : new Rectangle();

		return new Point(bounds.x + BORDER, bounds.y + BORDER_TOP);
	}

	/** The widget the client anchors its top left overlays at, the same one it uses itself. */
	private Widget viewportLayer()
	{
		if (client.isResized())
		{
			if (client.getVarbitValue(VarbitID.RESIZABLE_STONE_ARRANGEMENT) == 1)
			{
				return client.getWidget(InterfaceID.ToplevelPreEoc.HUD_CONTAINER_FRONT);
			}

			return client.getWidget(InterfaceID.ToplevelOsrsStretch.HUD_CONTAINER_FRONT);
		}

		return client.getWidget(InterfaceID.Toplevel.OVERLAY_HUD);
	}
}