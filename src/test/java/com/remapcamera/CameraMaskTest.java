package com.remapcamera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CameraMaskTest
{
	@Test
	public void theMaskMatchesWhatTheCameraPluginGivesTheClient()
	{
		// Right-click moves camera, and the menu does not block it: the client rotates with the
		// middle mouse button of the client, its own extra button, and the right click it rewrites
		// into a middle click.
		assertEquals((1 << 2) | (1 << 4), RemapCameraPlugin.cameraMask(true, false));
	}

	@Test
	public void withoutThatOptionTheClientHasNoMask()
	{
		assertEquals(0, RemapCameraPlugin.cameraMask(false, false));
		assertEquals(0, RemapCameraPlugin.cameraMask(null, null));
	}

	@Test
	public void aBlockingMenuTakesTheMaskAway()
	{
		// The Camera plugin still rewrites the right click into a middle click, but the client is
		// then on its keyboard path. That is the case the camera keys used to run away in.
		assertEquals(0, RemapCameraPlugin.cameraMask(true, true));
		assertEquals(0, RemapCameraPlugin.cameraMask(true, null));
	}

	@Test
	public void aRewrittenRightClickIsNotACameraDragWithoutTheMask()
	{
		assertFalse(RemapCameraPlugin.isCameraDrag(0, 2));
		assertFalse(RemapCameraPlugin.isCameraDrag(0, 3));
		assertTrue(RemapCameraPlugin.isCameraDrag(0, 4));
	}

	@Test
	public void withTheMaskOnlyTheButtonsOfTheMaskAreACameraDrag()
	{
		int mask = RemapCameraPlugin.cameraMask(true, false);

		assertTrue(RemapCameraPlugin.isCameraDrag(mask, 2));
		assertTrue(RemapCameraPlugin.isCameraDrag(mask, 4));
		assertFalse(RemapCameraPlugin.isCameraDrag(mask, 3));
	}

	@Test
	public void aimpossibleButtonIsNeverACameraDrag()
	{
		assertFalse(RemapCameraPlugin.isCameraDrag(-1, -1));
		assertFalse(RemapCameraPlugin.isCameraDrag(-1, 31));
	}
}