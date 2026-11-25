package com.remapcamera;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ClientCameraSpeedTest
{
	@Test
	public void aHeldCameraKeyWithNoMovementHoldsTheClientCameraStill()
	{
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
