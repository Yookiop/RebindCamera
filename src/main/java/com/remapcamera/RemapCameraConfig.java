package com.remapcamera;

import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ModifierlessKeybind;

@ConfigGroup("remapCamera")
public interface RemapCameraConfig extends Config
{
	@ConfigSection(
		name = "Camera Remapping",
		description = "Settings for remapping the camera",
		position = 0
	)
	String cameraSection = "camera";

	@ConfigItem(
		position = 1,
		keyName = "cameraRotateRemap",
		name = "Remap camera rotating key",
		description = "When enabled, holding the camera rotating key and moving the mouse will rotate the camera.",
		section = cameraSection
	)
	default boolean cameraRotateRemap()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "cameraRotateKey",
		name = "Camera rotating key",
		description = "Hold this key while moving the mouse to rotate the camera.",
		section = cameraSection
	)
	default ModifierlessKeybind cameraRotateKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_UNDEFINED, 0);
	}

	@ConfigItem(
		position = 3,
		keyName = "invertCamera",
		name = "Invert camera",
		description = "Inverts the camera rotation direction. Enable this if the camera moves the wrong way.",
		section = cameraSection
	)
	default boolean invertCamera()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "sensitivity",
			name = "Camera Sensitivity",
			description = "Mouse sensitivity when rotating the camera. Default is 180. \n 180 is roughly the same sensitivity as the default game middleclick",
			section = cameraSection
	)
	default int sensitivity()
	{
		return 180;
	}
}