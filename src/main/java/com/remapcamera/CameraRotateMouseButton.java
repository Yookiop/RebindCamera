package com.remapcamera;

/**
 * A mouse button that can hold the free look camera.
 *
 * <p>Only extra buttons are offered. The left and right button belong to the game, and the middle
 * button already rotates the camera itself, so using it here would rotate twice.
 */
enum CameraRotateMouseButton
{
	OFF("Off", 0),
	BUTTON_4("Mouse button 4", 4),
	BUTTON_5("Mouse button 5", 5);

	/** MouseEvent has no constants for buttons past BUTTON3. */
	private static final int FIRST_EXTRA_BUTTON = 4;

	private final String name;
	private final int button;

	CameraRotateMouseButton(String name, int button)
	{
		this.name = name;
		this.button = button;
	}

	/**
	 * Whether the pressed button is the one this option names. Left click, the middle button and
	 * right click never match, whatever the option is set to.
	 */
	boolean matches(int pressedButton)
	{
		return button >= FIRST_EXTRA_BUTTON && pressedButton == button;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
