package com.remapcamera;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseListener;

public class RemapCameraListener implements KeyListener, MouseListener
{
	@Inject
	private RemapCameraConfig config;
	@Inject
	private RemapCameraPlugin plugin;

	private volatile boolean cameraRotateKeyPressed = false;

	/**
	 * The camera keys that are currently held. They are tracked here, on the listener the client
	 * hands its key events to, and never by hooking the keyboard of the whole application.
	 */
	private final Set<Integer> heldKeys = ConcurrentHashMap.newKeySet();

	boolean isCameraRotateKeyPressed()
	{
		return cameraRotateKeyPressed;
	}

	boolean isCameraKeyHeld()
	{
		return !heldKeys.isEmpty();
	}

	/** Forgets every key, because their release is delivered to whichever window gets the focus. */
	void reset()
	{
		heldKeys.clear();
		cameraRotateKeyPressed = false;
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

			if (config.respectDialogues()){
   				if (plugin.isDialogOpen()){
					return;
				}
			}
			cameraRotateKeyPressed = true;
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		heldKeys.remove(e.getKeyCode());

		if (config.cameraRotateRemap() && cameraRotateKeyPressed && config.cameraRotateKey().matches(e))
		{
			cameraRotateKeyPressed = false;
			e.consume();
		}
	}

	@Override
	public void focusLost()
	{
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
		return e;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e)
	{
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
