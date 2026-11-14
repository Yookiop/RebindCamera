package com.remapcamera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class CameraKeyOverlay extends Overlay
{
	private final Client client;
	private final RemapCameraPlugin plugin;
	private final RemapCameraConfig config;

	@Inject
	CameraKeyOverlay(Client client, RemapCameraPlugin plugin, RemapCameraConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.TOP_LEFT);
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

		int x = config.indicatorX();
		int y = config.indicatorY() + chatboxOffset();

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
}