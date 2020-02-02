package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import frc.robot.logging.SyncLogger;
import frc.robot.oi.ControllerDriver;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Shifter;
import frc.robot.subsystems.Turret;
import frc.robot.utilities.Ports;
import frc.robot.commandgroups.AppeaseDuane;
import frc.robot.commands.*;
import frc.robot.devices.Lemonlight;
import frc.robot.devices.PigeonGyro;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {

  private CommandScheduler scheduler;

  private SyncLogger logger;

  private ControllerDriver controller1;

  // public just to make things work
  private Drivetrain drivetrain;
  private Shifter shifter;
  private Turret turret;

  private PigeonGyro gyro;
  private Lemonlight limelight;

  /**
   * The container for the robot.  Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    scheduler = CommandScheduler.getInstance();
    logger = new SyncLogger();

    // OI Devices
    controller1 = new ControllerDriver(logger, Ports.XBOX_PORT);

    // Sensors
    gyro = new PigeonGyro(Ports.PIGEON_IMU.port);

    // Subsystems
    drivetrain = new Drivetrain();
    shifter = new Shifter();
    turret = new Turret();

    limelight = new Lemonlight();

    scheduler.setDefaultCommand(drivetrain, new ArcadeDrive(drivetrain, controller1, shifter));

    logger.addElements(drivetrain, gyro, shifter);
    scheduler.setDefaultCommand(logger, logger);

    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings.  Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
   * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    controller1.leftBumper.whenPressed(new Shift(shifter));
    controller1.dPadRight.whenPressed(new GyroTurn(gyro, drivetrain, 90));
    controller1.dPadLeft.whenPressed(new GyroTurn(gyro, drivetrain, -90));

    controller1.buttonA.toggleWhenPressed(new StartEndCommand(
      () -> limelight.setLEDMode(Lemonlight.LEDModes.FORCE_ON),
      () -> limelight.setLEDMode(Lemonlight.LEDModes.FORCE_OFF)
    ));
  }

  public void teleopInit() {
    scheduler.schedule(new RunCommand(() -> System.out.println(limelight.getHorizontalOffset())));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return new AppeaseDuane(drivetrain, gyro);
  }
}
