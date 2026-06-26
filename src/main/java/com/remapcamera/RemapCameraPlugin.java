package com.remapcamera;

import com.google.inject.Provides;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
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
	public void onBeforeRender(BeforeRender event)
	{
		if (!inputListener.isCameraRotateKeyPressed())
		{
			lastMousePosition = null;
			return;
		}
		if (isDialogOpen()){
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

				// UPDATED: Yaw scale increased from 2047 to 16383
				client.setCameraYawTarget((client.getCameraYawTarget() + dx) & 16383);

				int currentPitch = client.getCameraPitchTarget();
				// UPDATED: Pitch bounds multiplied by 8 (Old: 128-383 | New: 1024-3064)
				client.setCameraPitchTarget(Math.max(1024, Math.min(3064, currentPitch - dy)));
			}
		}

		lastMousePosition = currentPos;
	}

	/**
	 * Check if a dialog is open that will grab numerical input, to prevent F-key remapping
	 * from triggering.
	 */
	public boolean isDialogOpen()
	{
		// Most chat dialogs with numerical input are added without the chatbox or its key listener being removed,
		// so chatboxFocused() is true. The chatbox onkey script uses the following logic to ignore key presses,
		// so we will use it too to not remap keys.
		return isHidden(InterfaceID.Chatbox.MES_LAYER_HIDE) || isHidden(InterfaceID.Chatbox.CHATDISPLAY)
				// We want to block camera remapping in the bank pin interface too, so it does not interfere with the
				// Keyboard Bankpin feature of the Bank plugin
				|| !isHidden(InterfaceID.BankpinKeypad.UNIVERSE);
	}

	private boolean isHidden(int component)
	{
		Widget w = client.getWidget(component);
		return w == null || w.isSelfHidden();
	}

	@Provides
	RemapCameraConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RemapCameraConfig.class);
	}
}