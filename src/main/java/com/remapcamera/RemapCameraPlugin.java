package com.remapcamera;

import com.google.inject.Provides;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Remap Camera Rotation",
	description = "Rotate the camera by holding a configurable key and moving the mouse",
	tags = {"camera", "rotate", "remap"}
)
public class RemapCameraPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private RemapCameraConfig config;

	@Inject
	private RemapCameraListener inputListener;

	private Point lastMousePosition;

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(inputListener);
		mouseManager.registerMouseListener(inputListener);
		lastMousePosition = null;
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(inputListener);
		mouseManager.unregisterMouseListener(inputListener);
		lastMousePosition = null;
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (!inputListener.isCameraRotateKeyPressed())
		{
			lastMousePosition = null;
			return;
		}

		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo == null)
		{
			return;
		}

		Point currentPos = pointerInfo.getLocation();

		if (lastMousePosition != null)
		{
			int dx = currentPos.x - lastMousePosition.x;
			int dy = currentPos.y - lastMousePosition.y;

			if (dx != 0 || dy != 0)
			{

				double scale = config.sensitivity() / 100.0;
				dx = (int) Math.round(dx * scale);
				dy = (int) Math.round(dy * scale);
				if (config.invertCamera())
				{
					dx = -dx;
					dy = -dy;
				}

				client.setCameraYawTarget((client.getCameraYawTarget() + dx) & 2047);

				int currentPitch = client.getCameraPitchTarget();
				client.setCameraPitchTarget(Math.max(128, Math.min(383, currentPitch - dy)));
			}
		}

		lastMousePosition = currentPos;
	}

	@Provides
	RemapCameraConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RemapCameraConfig.class);
	}
}