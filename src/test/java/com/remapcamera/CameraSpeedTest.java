package com.remapcamera;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CameraSpeedTest
{
	private static final int PITCH = 2048;

	private final CameraSpeed speed = new CameraSpeed();
	private int yaw = 1000;
	private int pitch = PITCH;

	/** One frame in which the client moved the camera by {@code movement}. */
	private void frame(int movement, double scale)
	{
		frame(movement, scale, false, false);
	}

	private void frame(int movement, double scale, boolean clientDrivesCamera, boolean holdCamera)
	{
		yaw += movement;
		speed.update(yaw, pitch, scale, clientDrivesCamera, holdCamera);
		yaw = speed.yaw();
		pitch = speed.pitch();
	}

	@Test
	public void theFirstFrameTakesTheCameraPosition()
	{
		speed.update(1234, PITCH, 0.5, false, false);

		assertEquals(1234, speed.yaw());
		assertEquals(PITCH, speed.pitch());
	}

	@Test
	public void fullSpeedFollowsTheClient()
	{
		frame(0, 1);

		for (int i = 0; i < 20; i++)
		{
			frame(24, 1);
		}

		assertEquals(1000 + 20 * 24, yaw);
	}

	@Test
	public void zeroSpeedHoldsTheCamera()
	{
		frame(0, 0);

		for (int i = 0; i < 20; i++)
		{
			frame(24, 0);
		}

		assertEquals(1000, yaw);
	}

	@Test
	public void halfSpeedKeepsHalfOfTheMovement()
	{
		frame(0, 0.5);

		for (int i = 0; i < 20; i++)
		{
			frame(24, 0.5);
		}

		assertEquals(1000 + 20 * 12, yaw);
	}

	@Test
	public void theRemainderIsKeptForTheNextFrame()
	{
		frame(0, 0.1);

		// 24 * 0.1 is 2.4 units, so every frame has to keep a fraction instead of rounding it away.
		for (int i = 0; i < 10; i++)
		{
			frame(24, 0.1);
		}

		assertEquals(1000 + 24, yaw);
	}

	@Test
	public void theCameraStaysPutWhileTheCameraIsRotated()
	{
		frame(0, 0.5);

		for (int i = 0; i < 20; i++)
		{
			frame(24, 0.5);
		}

		int held = yaw;

		for (int i = 0; i < 10; i++)
		{
			frame(24, 0.5, false, true);
		}

		assertEquals(held, yaw);
	}

	@Test
	public void theMouseCameraIsNeverScaled()
	{
		frame(0, 0);

		frame(400, 0, true, false);

		assertEquals(1000 + 400, yaw);
	}

	@Test
	public void theYawWrapsAroundTheCircle()
	{
		yaw = 16380;
		frame(0, 1);

		frame(24, 1);

		assertEquals(16380 + 24 - 16384, yaw);
	}

	/** One frame in which the client moved the camera pitch by {@code movement}. */
	private void pitchFrame(int movement, double scale)
	{
		pitch += movement;
		speed.update(yaw, pitch, scale, false, false);
		yaw = speed.yaw();
		pitch = speed.pitch();
	}

	@Test
	public void thePitchStaysInsideTheClientLimits()
	{
		pitch = 3060;
		pitchFrame(0, 1);
		pitchFrame(24, 1);

		assertEquals(3064, pitch);
	}
}
