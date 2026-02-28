package com.remapcamera;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RemapCameraPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RemapCameraPlugin.class);
		RuneLite.main(args);
	}
}
