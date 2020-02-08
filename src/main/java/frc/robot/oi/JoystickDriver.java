package frc.robot.oi;

import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.logging.LoggerRelations;
import frc.robot.logging.SyncLogger;
import frc.robot.utilities.Ports;

/**
 * Wrapper class for basic joystick functionality
 */
public class JoystickDriver extends GenericDriver implements Subsystem {

	public LoggerButton
	button1,
	button2,
	button3,
	button4,
	trigger;

	public LoggerAxis
	axisX,
	axisY;

	public JoystickDriver(Ports port, SyncLogger logger) {
		this.port = port.port;
		this.logger = logger;

		//TODO - make sure ports are correct
		button1 = generateLoggerButton(1, LoggerRelations.PLACEHOLDER);
		button2 = generateLoggerButton(2, LoggerRelations.PLACEHOLDER);
		button3 = generateLoggerButton(3, LoggerRelations.PLACEHOLDER);
		button4 = generateLoggerButton(4, LoggerRelations.PLACEHOLDER);
		trigger = generateLoggerButton(5, LoggerRelations.PLACEHOLDER);

		axisX = generateLoggerAxis(1, LoggerRelations.PLACEHOLDER);
		axisY = generateLoggerAxis(2, LoggerRelations.PLACEHOLDER);
	}
}