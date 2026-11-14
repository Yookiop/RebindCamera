package com.remapcamera;

import com.google.inject.Provides;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
		name = "Remap Camera Rotation",
		description = "Rotate the camera by holding a configurable key and moving the mouse, and" +
			" adjust the speed of the arrow key camera",
		tags = {"camera", "rotate", "speed", "remap"}
)
public class RemapCameraPlugin extends Plugin
{
	private static final int YAW_UNITS = 16384;
	private static final int PITCH_MIN = 1024;
	private static final int PITCH_MAX = 3064;
	private static final double MAX_SPEED = 20;

	/** Used to mirror the camera mouse button mask of the built-in Camera plugin. */
	private static final String CAMERA_CONFIG_GROUP = "camera";

	private static final int CAMERA_MOUSE_BUTTONS =
		(1 << MouseEvent.BUTTON2) | (1 << 4 /* the middle mouse button of the client */);

	private static final String CONFIG_GROUP =
		RemapCameraConfig.class.getAnnotation(ConfigGroup.class).value();

	@Inject
	private Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RemapCameraConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private RemapCameraListener inputListener;

	@Inject
	private CameraKeyOverlay keyOverlay;

	private final CameraSpeed cameraSpeed = new CameraSpeed();

	private final Set<Integer> heldKeys = ConcurrentHashMap.newKeySet();

	/**
	 * Watches the keyboard before the game and before other plugins, so the arrow keys are seen even
	 * when something else consumes them. The event is never consumed here.
	 */
	private final KeyEventDispatcher keyTracker = this::trackKey;

	private final FocusListener canvasFocusListener = new FocusAdapter()
	{
		@Override
		public void focusLost(FocusEvent event)
		{
			// The release of a key that is held while the game loses focus is delivered to the window
			// that gets the focus instead, so it would otherwise stay stuck.
			heldKeys.clear();
		}
	};

	private KeyboardFocusManager keyboardFocusManager;

	private Point lastMousePosition;

	private net.runelite.api.Point lastCanvasMouse;

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(inputListener);
		mouseManager.registerMouseListener(inputListener);
		overlayManager.add(keyOverlay);
		lastMousePosition = null;
		heldKeys.clear();

		SwingUtilities.invokeLater(() ->
		{
			Component canvas = client.getCanvas();

			if (canvas != null)
			{
				canvas.addFocusListener(canvasFocusListener);
			}

			keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			keyboardFocusManager.addKeyEventDispatcher(keyTracker);
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(inputListener);
		mouseManager.unregisterMouseListener(inputListener);
		overlayManager.remove(keyOverlay);
		lastMousePosition = null;
		heldKeys.clear();

		// Queued behind the startup task, so an add and a remove can never be reordered.
		SwingUtilities.invokeLater(() ->
		{
			Component canvas = client.getCanvas();

			if (canvas != null)
			{
				canvas.removeFocusListener(canvasFocusListener);
			}

			if (keyboardFocusManager != null)
			{
				keyboardFocusManager.removeKeyEventDispatcher(keyTracker);
				keyboardFocusManager = null;
			}
		});
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		boolean rotating = inputListener.isCameraRotateKeyPressed() && !isDialogOpen();
		boolean scaleKeys = config.cameraKeysEnabled();

		if (!scaleKeys)
		{
			// With the camera key configuration off the plugin behaves like it did before those
			// options existed: the arrow keys are left to the client and only the rotation is written.
			cameraSpeed.reset();

			if (!rotating)
			{
				lastMousePosition = null;

				return;
			}
		}

		int yaw = client.getCameraYawTarget();
		int pitch = client.getCameraPitchTarget();

		if (scaleKeys)
		{
			// The arrow keys are scaled, not blocked, so the game keeps seeing them as input. While
			// the camera is rotated, and while the client is dragging its own camera with the mouse,
			// only that rotation may move the camera.
			boolean mouseMoved = mouseMoved();
			boolean clientDrivesCamera = clientCameraDrag() && (mouseMoved || heldKeys.isEmpty());

			cameraSpeed.update(yaw, pitch, cameraSpeed(), clientDrivesCamera,
				rotating && !clientDrivesCamera);
			yaw = cameraSpeed.yaw();
			pitch = cameraSpeed.pitch();
		}

		if (!rotating)
		{
			lastMousePosition = null;
		}
		else
		{
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			Point currentPos = pointerInfo != null ? pointerInfo.getLocation() : null;

			if (currentPos != null)
			{
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

						yaw = (yaw + dx) & (YAW_UNITS - 1);
						pitch = Math.max(PITCH_MIN, Math.min(PITCH_MAX, pitch - dy));
					}
				}

				lastMousePosition = currentPos;
			}
		}

		client.setCameraYawTarget(yaw);
		client.setCameraPitchTarget(pitch);

		if (scaleKeys)
		{
			cameraSpeed.written(yaw, pitch);
		}
	}

	/**
	 * Respecting dialogues already means the plugin follows the interface state of the game, so the
	 * indicator has to follow the chatbox too. Turning the option back off does not turn this off
	 * again, so a manual choice keeps standing.
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup())
			&& "respectDialogues".equals(event.getKey())
			&& Boolean.parseBoolean(event.getNewValue())
			&& !config.indicatorRespectChatbox())
		{
			configManager.setConfiguration(CONFIG_GROUP, "indicatorRespectChatbox", true);
		}
	}

	/** The configured camera key speed, clamped to the range the config allows. */
	private double cameraSpeed()
	{
		double speed = config.cameraSpeed();

		if (!(speed > 0))
		{
			return 0;
		}

		return Math.min(speed, MAX_SPEED);
	}

	/** The client moves its camera from the mouse alone while a camera mouse button is held. */
	private boolean clientCameraDrag()
	{
		return isCameraDrag(cameraMask(), client.getMouseCurrentButton());
	}

	/**
	 * The decision the client itself makes: with a mask, the bit of the button it sees; without one
	 * only its own extra mouse button. The button it sees is not the button that was pressed when the
	 * built-in Camera plugin rewrote a right click into a middle click, so the mask has to be taken
	 * into account to tell a camera drag from keyboard movement.
	 */
	static boolean isCameraDrag(int mask, int button)
	{
		if (button < 0 || button > 30)
		{
			return false;
		}

		if (mask != 0)
		{
			return ((mask >> button) & 1) != 0;
		}

		return button == 4;
	}

	/**
	 * The camera mouse button mask the built-in Camera plugin gives the client, mirroring that
	 * plugin's own config. It decides whether the client moves its camera from the mouse or from the
	 * keys, so the two have to agree.
	 */
	private int cameraMask()
	{
		return cameraMask(
			configManager.getConfiguration(CAMERA_CONFIG_GROUP, "rightClickMovesCamera", Boolean.class),
			configManager.getConfiguration(CAMERA_CONFIG_GROUP, "rightClickMenuBlocksCamera", Boolean.class));
	}

	static int cameraMask(Boolean rightClickMovesCamera, Boolean rightClickMenuBlocksCamera)
	{
		boolean movesCamera = rightClickMovesCamera != null && rightClickMovesCamera;
		boolean menuBlocks = rightClickMenuBlocksCamera == null || rightClickMenuBlocksCamera;

		return movesCamera && !menuBlocks ? CAMERA_MOUSE_BUTTONS : 0;
	}

	/**
	 * Whether the mouse moved since the previous frame. A camera drag needs mouse movement, so a
	 * movement of the camera while the mouse is still cannot come from the mouse.
	 */
	private boolean mouseMoved()
	{
		net.runelite.api.Point position = client.getMouseCanvasPosition();
		boolean moved = lastCanvasMouse == null || !lastCanvasMouse.equals(position);
		lastCanvasMouse = position;

		return moved;
	}

	/** Whether the plugin currently sees a camera key, for the info overlay. */
	boolean cameraKeysHeld()
	{
		return !heldKeys.isEmpty();
	}

	private boolean trackKey(KeyEvent event)
	{
		int keyCode = event.getKeyCode();

		if (event.getID() == KeyEvent.KEY_RELEASED)
		{
			heldKeys.remove(keyCode);

			return false;
		}

		if (event.getID() != KeyEvent.KEY_PRESSED || !gameCanvasFocused() || isTyping())
		{
			return false;
		}

		if (isCameraKey(keyCode))
		{
			heldKeys.add(keyCode);
		}

		return false;
	}

	private boolean gameCanvasFocused()
	{
		Component canvas = client.getCanvas();

		if (canvas == null)
		{
			return false;
		}

		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		return focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, canvas);
	}

	private boolean isTyping()
	{
		return client.getFocusedInputFieldWidget() != null;
	}

	private static boolean isCameraKey(int keyCode)
	{
		return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN
			|| keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT;
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