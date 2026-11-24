package com.remapcamera;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Proxy;
import org.junit.Test;

public class CameraRotateMouseButtonTest
{
	@Test
	public void onlyTheConfiguredExtraButtonMatches()
	{
		assertTrue(CameraRotateMouseButton.BUTTON_4.matches(4));
		assertTrue(CameraRotateMouseButton.BUTTON_5.matches(5));

		assertFalse(CameraRotateMouseButton.BUTTON_4.matches(5));
		assertFalse(CameraRotateMouseButton.BUTTON_5.matches(4));
	}

	@Test
	public void theButtonsOfTheGameNeverMatch()
	{
		for (CameraRotateMouseButton option : CameraRotateMouseButton.values())
		{
			assertFalse(option.matches(0));
			assertFalse(option.matches(1));
			assertFalse(option.matches(2));
			assertFalse(option.matches(3));
		}
	}

	@Test
	public void offMatchesNothing()
	{
		assertFalse(CameraRotateMouseButton.OFF.matches(4));
		assertFalse(CameraRotateMouseButton.OFF.matches(5));
	}

	/**
	 * RuneLite hands the plugin a dynamic proxy for the config interface, and that proxy class lives
	 * in its own package. A config item type it cannot access throws an IllegalAccessError on every
	 * call, which is why the enum has to be public.
	 */
	@Test
	public void theConfigProxyReturnsTheOption()
	{
		RemapCameraConfig config = (RemapCameraConfig) Proxy.newProxyInstance(
			RemapCameraConfig.class.getClassLoader(),
			new Class<?>[]
			{
				RemapCameraConfig.class
			},
			(proxy, method, args) -> CameraRotateMouseButton.BUTTON_5);

		assertSame(CameraRotateMouseButton.BUTTON_5, config.cameraRotateMouseButton());
	}
}
