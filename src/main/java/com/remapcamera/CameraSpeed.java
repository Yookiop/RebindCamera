package com.remapcamera;

/**
 * Keeps the arrow key camera at a fraction of the movement the client makes.
 *
 * <p>The keys are not blocked: the client keeps receiving and acting on them, and the movement it
 * made since the previous frame is scaled here. That matters because the game uses that same input
 * to decide whether the player is active, which an escape crystal's inactivity timer relies on.
 *
 * <p>Nothing is derived from a fixed number of units per second: the client moves the camera along
 * its own acceleration curve, so the measured movement is the only truth. A fraction that is left
 * over is kept for the next frame, so even a speed far below one camera unit per frame still moves
 * the camera.
 *
 * <p>Only movement of the keys is scaled. The compass sets an absolute target of its own, which is
 * passed through untouched so that one click still points the camera north.
 */
class CameraSpeed
{
	private static final int YAW_UNITS = 16384;
	private static final int PITCH_MIN = 1024;
	private static final int PITCH_MAX = 3064;

	private int yaw = -1;
	private int pitch;
	private double yawRemainder;
	private double pitchRemainder;

	/** The target the client should get, after {@link #update}. */
	int yaw()
	{
		return yaw;
	}

	int pitch()
	{
		return pitch;
	}

	/** Forgets the camera position, so that the next frame takes it over again. */
	void reset()
	{
		yaw = -1;
		yawRemainder = 0;
		pitchRemainder = 0;
	}

	/** Remembers a target that was written without the plugin scaling it. */
	void written(int yawTarget, int pitchTarget)
	{
		yaw = yawTarget;
		pitch = pitchTarget;
	}

	/**
	 * Scales the movement the client made since the previous frame.
	 *
	 * @param yawTarget          the camera target the client currently has
	 * @param pitchTarget        the camera target the client currently has
	 * @param speed              the configured fraction of the client's keyboard camera speed
	 * @param clientDrivesCamera the client is moving the camera from the mouse, so its target is the
	 *                           truth and nothing may be scaled
	 * @param holdCamera         the camera is being rotated with the rotate key, so only that
	 *                           rotation may move the camera
	 * @param compassClick       the compass was clicked, which sets an absolute target that must
	 *                           not be scaled, or it would take several clicks to point north
	 */
	void update(int yawTarget, int pitchTarget, double speed, boolean clientDrivesCamera,
		boolean holdCamera, boolean compassClick)
	{
		if (yaw < 0 || clientDrivesCamera)
		{
			yawRemainder = 0;
			pitchRemainder = 0;
			written(yawTarget, pitchTarget);

			return;
		}

		if (holdCamera)
		{
			yawRemainder = 0;
			pitchRemainder = 0;

			if (compassClick)
			{
				yaw = yawTarget & (YAW_UNITS - 1);
			}

			return;
		}

		if (compassClick)
		{
			yawRemainder = 0;
			yaw = yawTarget & (YAW_UNITS - 1);
		}
		else
		{
			int yawDelta = shortestRotation(yaw, yawTarget);

			yaw = (yaw + scaled(yawDelta, speed, true)) & (YAW_UNITS - 1);
		}

		int pitchDelta = pitchTarget - pitch;

		pitch = clamp(pitch + scaled(pitchDelta, speed, false));
	}

	/**
	 * The part of a movement that is kept. A single frame can be smaller than one camera unit, so
	 * the remainder is carried to the next frame instead of being rounded away.
	 */
	private int scaled(int delta, double speed, boolean yawAxis)
	{
		double remainder = yawAxis ? yawRemainder : pitchRemainder;
		double value = delta * speed + remainder;
		int step = (int) value;

		if (yawAxis)
		{
			yawRemainder = value - step;
		}
		else
		{
			pitchRemainder = value - step;
		}

		return step;
	}

	private static int clamp(int pitch)
	{
		return Math.max(PITCH_MIN, Math.min(PITCH_MAX, pitch));
	}

	private static int shortestRotation(int from, int to)
	{
		int delta = (to - from) % YAW_UNITS;

		if (delta > YAW_UNITS / 2)
		{
			delta -= YAW_UNITS;
		}
		else if (delta < -YAW_UNITS / 2)
		{
			delta += YAW_UNITS;
		}

		return delta;
	}
}