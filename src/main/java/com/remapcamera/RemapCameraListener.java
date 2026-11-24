package com.remapcamera;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemapCameraListener implements KeyListener, MouseListener
{
	private static final Logger log = LoggerFactory.getLogger(RemapCameraListener.class);

	/** The compass of every top level layout; only the one of the current layout exists. */
	private static final int[] COMPASS_WIDGETS =
	{
		InterfaceID.ToplevelOsrsStretch.COMPASSCLICK,
		InterfaceID.ToplevelPreEoc.COMPASSCLICK,
		InterfaceID.Toplevel.COMPASSCLICK,
		InterfaceID.ToplevelOsm.COMPASSCLICK
	};

	/** How long the target the compass sets is left alone, so it is never missed. */
	private static final long COMPASS_CLICK_WINDOW_MS = 200;

	@Inject
	private Client client;
	@Inject
	private RemapCameraConfig config;
	@Inject
	private RemapCameraPlugin plugin;

	private volatile boolean cameraRotateKeyPressed = false;
	private volatile boolean cameraRotateButtonPressed = false;

	/**
	 * The camera keys that are currently held. They are tracked here, on the listener the client
	 * hands its key events to, and never by hooking the keyboard of the whole application.
	 */
	private final Set<Integer> heldKeys = ConcurrentHashMap.newKeySet();

	private volatile long compassClickUntil;

	/** Whether the free look camera should rotate, by the configured key or by the mouse button. */
	boolean isCameraRotateHeld()
	{
		return cameraRotateKeyPressed || cameraRotateButtonPressed;
	}

	boolean isCameraRotateKeyHeld()
	{
		return cameraRotateKeyPressed;
	}

	boolean isCameraRotateButtonHeld()
	{
		return cameraRotateButtonPressed;
	}

	boolean isCameraKeyHeld()
	{
		return !heldKeys.isEmpty();
	}

	/**
	 * Whether the compass was clicked just now. The compass sets the camera target to an absolute
	 * angle instead of moving it like a key does, so the target has to be left alone right after
	 * the click; scaling it would make the camera turn only part of the way to north.
	 */
	boolean isCompassClickActive()
	{
		return System.nanoTime() < compassClickUntil;
	}

	/** Starts the window in which the target the compass sets is not scaled. */
	void compassClicked()
	{
		compassClickUntil = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(COMPASS_CLICK_WINDOW_MS);
	}

	/** Forgets every key, because their release is delivered to whichever window gets the focus. */
	void reset()
	{
		heldKeys.clear();
		resetRotation();
		compassClickUntil = 0;
	}

	/**
	 * Forgets the rotation only. The configured key or button can change while one of them is held,
	 * and its release would then never be recognised.
	 */
	void resetRotation()
	{
		if (cameraRotateKeyPressed || cameraRotateButtonPressed)
		{
			log.debug("forgetting rotation (key {} / button {})", cameraRotateKeyPressed, cameraRotateButtonPressed);
		}

		cameraRotateKeyPressed = false;
		cameraRotateButtonPressed = false;
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!plugin.isTyping() && isCameraKey(e.getKeyCode()))
		{
			heldKeys.add(e.getKeyCode());
		}

		if (config.cameraRotateRemap() && config.cameraRotateKey().matches(e))
		{
			// A held key repeats this event, so only the first press is worth a log line.
			boolean firstPress = !cameraRotateKeyPressed;

			if (config.respectDialogues() && plugin.isDialogOpen())
			{
				if (firstPress)
				{
					log.debug("rotation key {} pressed but a dialogue is open", KeyEvent.getKeyText(e.getKeyCode()));
				}

				return;
			}

			// Observe only, so the event is not consumed: the client keeps its own key state either
			// way, and consuming would only take the key's activity away from the game.
			if (firstPress)
			{
				log.debug("rotation key {} held", KeyEvent.getKeyText(e.getKeyCode()));
			}

			cameraRotateKeyPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		heldKeys.remove(e.getKeyCode());

		if (config.cameraRotateRemap() && cameraRotateKeyPressed && config.cameraRotateKey().matches(e))
		{
			cameraRotateKeyPressed = false;
			log.debug("rotation key {} released", KeyEvent.getKeyText(e.getKeyCode()));
		}
	}

	@Override
	public void focusLost()
	{
		log.debug("focus lost, forgetting every held key");
		reset();
	}

	private static boolean isCameraKey(int keyCode)
	{
		return keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN
			|| keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT;
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent e)
	{
		if (isCompassClick(e.getX(), e.getY()))
		{
			compassClicked();
		}

		CameraRotateMouseButton configured = config.cameraRotateMouseButton();
		boolean match = configured.matches(e.getButton());
		boolean dialogueBlocks = config.respectDialogues() && plugin.isDialogOpen();

		if (match && !dialogueBlocks)
		{
			// The event is left as it is: nothing is consumed and no button is rewritten, so the
			// game sees exactly the click it would see without the plugin.
			cameraRotateButtonPressed = true;
		}

		log.debug("mouse button {} pressed: option {}, match {}, dialogue blocks {}, consumed {}",
			e.getButton(), configured, match, dialogueBlocks, e.isConsumed());

		return e;
	}

	private boolean isCompassClick(int x, int y)
	{
		for (int widgetId : COMPASS_WIDGETS)
		{
			Widget compass = client.getWidget(widgetId);

			if (compass != null && compass.getBounds().contains(x, y))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e)
	{
		boolean match = config.cameraRotateMouseButton().matches(e.getButton());

		if (match)
		{
			cameraRotateButtonPressed = false;
		}

		log.debug("mouse button {} released: match {}, consumed {}", e.getButton(), match, e.isConsumed());

		return e;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent e)
	{
		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e)
	{
		return e;
	}
}
