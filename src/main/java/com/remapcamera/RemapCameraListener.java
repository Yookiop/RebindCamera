package com.remapcamera;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseListener;

public class RemapCameraListener implements KeyListener, MouseListener
{
	@Inject
	private RemapCameraConfig config;

	private boolean cameraRotateKeyPressed = false;
	private int lastMouseX = -1;
	private int lastMouseY = -1;

	// Accumulated yaw/pitch delta (sensitivity pre-multiplied), drained on the client
	// thread in RemapCameraPlugin#onClientTick to avoid mutating game state from the AWT thread.
	final AtomicInteger pendingDx = new AtomicInteger();
	final AtomicInteger pendingDy = new AtomicInteger();

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.cameraRotateRemap() && config.cameraRotateKey().matches(e))
		{
			cameraRotateKeyPressed = true;
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (config.cameraRotateRemap() && cameraRotateKeyPressed && config.cameraRotateKey().matches(e))
		{
			cameraRotateKeyPressed = false;
			lastMouseX = -1;
			lastMouseY = -1;
			e.consume();
		}
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
		if (cameraRotateKeyPressed)
		{
			accumulateDelta(e.getX(), e.getY());
		}
		lastMouseX = e.getX();
		lastMouseY = e.getY();
		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e)
	{
		// Only track position while a mouse button is held; do not accumulate camera delta
		// here to avoid conflicting with the game's own right-click camera drag.
		lastMouseX = e.getX();
		lastMouseY = e.getY();
		return e;
	}

	private void accumulateDelta(int x, int y)
	{
		if (lastMouseX == -1 || lastMouseY == -1)
		{
			return;
		}

		int dx = x - lastMouseX;
		int dy = y - lastMouseY;

		if (dx == 0 && dy == 0)
		{
			return;
		}

		pendingDx.addAndGet(dx);
		pendingDy.addAndGet(dy);
	}
}
