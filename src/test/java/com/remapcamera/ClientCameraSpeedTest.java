package com.remapcamera;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ClientCameraSpeedTest
{
	@Test
	public void aHeldCameraKeyWithNoMovementHoldsTheClientCameraStill()
	{
		// The client keeps moving the camera after the plugin wrote the target, and it scales that
		// movement with its camera speed; that leftover movement is what shakes the view. With the
		// camera keys on no movement the client is held still for as long as a key is held.
		assertTrue(RemapCameraPlugin.slowDownClientCamera(0, true));
	}

	@Test
	public void aCameraKeySpeedAboveZeroLeavesTheClientAlone()
	{
		assertFalse(RemapCameraPlugin.slowDownClientCamera(0.001, true));
		assertFalse(RemapCameraPlugin.slowDownClientCamera(1, true));
	}

	@Test
	public void withoutACameraKeyTheClientKeepsItsSpeed()
	{
		assertFalse(RemapCameraPlugin.slowDownClientCamera(0, false));
	}
}
