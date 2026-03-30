package com.remapcamera;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseListener;

public class RemapCameraListener implements KeyListener, MouseListener
{
	@Inject
	private RemapCameraConfig config;

	private volatile boolean cameraRotateKeyPressed = false;

	boolean isCameraRotateKeyPressed()
	{
		return cameraRotateKeyPressed;
	}

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
		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e)
	{
		return e;
	}
}
