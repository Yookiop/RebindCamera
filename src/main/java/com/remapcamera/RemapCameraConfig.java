package com.remapcamera;

import java.awt.Color;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.ModifierlessKeybind;

@ConfigGroup("remapCamera")
public interface RemapCameraConfig extends Config
{
	@ConfigSection(
			name = "Free look camera",
			description = "Settings for remapping the free look camera",
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
			keyName = "respectDialogues",
			name = "Respect dialogues",
			description = "The plugin will respect dialogues and not run when a dialogue or interface is open.",
			section = cameraSection
	)
	default boolean respectDialogues()
	{
		return false;
	}

	@Range(min = 1, max = 10000)
	@ConfigItem(
			position = 5,
			keyName = "sensitivity",
			name = "Camera sensitivity",
			description = "Mouse sensitivity when rotating the camera. Default is 1440 which is around the same as middle mouse click camera rotation.",
			section = cameraSection
	)
	default int sensitivity()
	{
		return 1440;
	}

	@ConfigItem(
			position = 6,
			keyName = "cameraRotateMouseButton",
			name = "Camera rotating mouse button",
			description = "Rotate the camera while this mouse button is held, as an alternative to the " +
					"camera rotating key.",
			section = cameraSection
	)
	default CameraRotateMouseButton cameraRotateMouseButton()
	{
		return CameraRotateMouseButton.OFF;
	}

	@ConfigSection(
			name = "Camera keys",
			description = "Settings for the arrow key camera. Nothing below does anything while it is off.",
			position = 1,
			closedByDefault = true
	)
	String cameraKeysSection = "cameraKeys";

	@ConfigItem(
			position = 1,
			keyName = "cameraKeysEnabled",
			name = "Use camera key configuration",
			description = "Enables everything below it. While it is off the plugin does not touch the " +
					"arrow key camera at all and only the free look camera of the section above is used.",
			section = cameraKeysSection
	)
	default boolean cameraKeysEnabled()
	{
		return false;
	}

	@Range(min = 0, max = 20)
	@ConfigItem(
			position = 2,
			keyName = "cameraSpeed",
			name = "Camera key speed",
			description = "Speed of the arrow key camera as a fraction of the normal speed: 1 = normal",
			section = cameraKeysSection
	)
	default double cameraSpeed()
	{
		return 1;
	}

	@ConfigItem(
			position = 3,
			keyName = "keyIndicatorEnabled",
			name = "Show camera key indicator",
			description = "Shows the indicator while a camera key is held.",
			section = cameraKeysSection
	)
	default boolean keyIndicatorEnabled()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "indicatorText",
			name = "Indicator text",
			description = "The text of the indicator, for example a check mark.",
			section = cameraKeysSection
	)
	default String indicatorText()
	{
		return "✓";
	}

	@Alpha
	@ConfigItem(
			position = 5,
			keyName = "indicatorColor",
			name = "Indicator color",
			description = "Colour of the indicator text.",
			section = cameraKeysSection
	)
	default Color indicatorColor()
	{
		return Color.GREEN;
	}

	@Range(min = 8, max = 128)
	@ConfigItem(
			position = 6,
			keyName = "indicatorSize",
			name = "Indicator size",
			description = "Font size of the indicator text.",
			section = cameraKeysSection
	)
	default int indicatorSize()
	{
		return 16;
	}

	@Range(min = -1000, max = 1000)
	@ConfigItem(
			position = 7,
			keyName = "indicatorX",
			name = "Indicator X offset",
			description = "Horizontal offset from the top left of the game viewport, in pixels.",
			section = cameraKeysSection
	)
	default int indicatorX()
	{
		return 0;
	}

	@Range(min = -1000, max = 1000)
	@ConfigItem(
			position = 8,
			keyName = "indicatorY",
			name = "Indicator Y offset",
			description = "Vertical offset from the top left of the game viewport, in pixels.",
			section = cameraKeysSection
	)
	default int indicatorY()
	{
		return 0;
	}

	@ConfigItem(
			position = 9,
			keyName = "indicatorRespectChatbox",
			name = "Respect chatbox",
			description = "Moves the indicator while the chatbox is open, because the game moves its " +
					"own overlays then too. Use the offset below to set how far.",
			section = cameraKeysSection
	)
	default boolean indicatorRespectChatbox()
	{
		return false;
	}

	@Range(min = -1000, max = 1000)
	@ConfigItem(
			position = 10,
			keyName = "indicatorChatboxY",
			name = "Chatbox Y offset",
			description = "How many pixels the indicator moves while the chatbox is open. A negative " +
					"number moves it up.",
			section = cameraKeysSection
	)
	default int indicatorChatboxY()
	{
		return 0;
	}
}