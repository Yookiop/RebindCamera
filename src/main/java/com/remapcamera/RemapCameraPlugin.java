package com.remapcamera;

import com.google.inject.Provides;
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
	private RemapCameraListener inputListener;

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(inputListener);
		mouseManager.registerMouseListener(inputListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(inputListener);
		mouseManager.unregisterMouseListener(inputListener);
		inputListener.pendingDx.set(0);
		inputListener.pendingDy.set(0);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		int dx = inputListener.pendingDx.getAndSet(0);
		int dy = inputListener.pendingDy.getAndSet(0);

		if (dx == 0 && dy == 0)
		{
			return;
		}

		// Camera yaw wraps 0-2047; pitch is clamped 128-383 (standard OSRS limits)
		int newYaw = (client.getCameraYawTarget() + dx) & 2047;
		int newPitch = Math.max(128, Math.min(383, client.getCameraPitchTarget() - dy));

		client.setCameraYawTarget(newYaw);
		client.setCameraPitchTarget(newPitch);
	}

	@Provides
	RemapCameraConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RemapCameraConfig.class);
	}
}
