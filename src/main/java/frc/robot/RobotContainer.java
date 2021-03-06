package frc.robot;

import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import frc.robot.logging.SyncLogger;
import frc.robot.oi.ControllerDriver;
import frc.robot.oi.JoystickDriver;
import frc.robot.oi.LaunchpadDriver;
import frc.robot.subsystems.ClimberArm;
import frc.robot.subsystems.ClimberPneumatics;
import frc.robot.subsystems.Conveyor;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.IntakeArm;
import frc.robot.subsystems.Shifter;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Turret;
import frc.robot.subsystems.ClimberArm.Sides;
import frc.robot.utilities.Ports;
import frc.robot.commands.climb.ClimbSequence;
import frc.robot.commands.climb.ClimberArmMO;
import frc.robot.commands.climb.RaiseArm;
import frc.robot.commands.conveyor.ConveyorMO;
import frc.robot.commands.drivetrain.ArcadeDrive;
import frc.robot.commands.drivetrain.EncoderDrive;
import frc.robot.commands.intake.IntakeArmDefault;
import frc.robot.commands.intake.IntakeArmMO;
import frc.robot.commands.intake.SetDown;
import frc.robot.commands.intake.SetLoad;
import frc.robot.commands.intake.SetUp;
import frc.robot.devices.LEDs;
import frc.robot.devices.LEDs.LEDRange;
import frc.robot.devices.Lemonlight;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {

    private CommandScheduler scheduler;

    private SyncLogger logger;

    private ControllerDriver controller1;
    private LaunchpadDriver launchpad;
    private JoystickDriver joystick;

    private LEDs leds;
    private Compressor compressor;

    private Drivetrain drivetrain;
    private Shifter shifter;
    private Conveyor conveyor;
    private IntakeArm intakeArm;
    //private Shooter shooter;
    private ClimberArm leftArm, rightArm;
    //private Turret turret;
    private ClimberPneumatics climberPneumatics;

    //private Lemonlight limelight;
    private ColorSensorV3 colorSensor;

    //private DoubleSolenoid buddySolenoid;
    //private Solenoid lock;

    private Command autoInit;
    private Command teleInit;

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        scheduler = CommandScheduler.getInstance();
        logger = new SyncLogger();

        leds = new LEDs();
        LEDRange shifterRange = leds.getAllLedsRangeController();

        controller1 = new ControllerDriver(Ports.XBOX_PORT, logger);
        launchpad = new LaunchpadDriver(Ports.LAUNCHPAD_PORT, logger);
        joystick = new JoystickDriver(Ports.JOYSTICK_PORT, logger);

        compressor = new Compressor(Ports.PCM_1);
        compressor.setClosedLoopControl(true);

        drivetrain = new Drivetrain();
        shifter = new Shifter(shifterRange);
        conveyor = new Conveyor();
        intakeArm = new IntakeArm();
        //shooter = new Shooter();
        leftArm = new ClimberArm(Sides.LEFT);
        rightArm = new ClimberArm(Sides.RIGHT);
        // turret = new Turret();
        climberPneumatics = new ClimberPneumatics();

        // buddySolenoid = new DoubleSolenoid(Ports.PCM_1, Ports.OPEN_CLAMP,
        // Ports.CLOSE_CLAMP);

        // gyro = new PigeonGyro(Ports.PIGEON_IMU.port);
        // limelight = new Lemonlight();
        colorSensor = new ColorSensorV3(Port.kOnboard);

        setDefaultCommands();
        configureButtonBindings();

        logger.addElements(drivetrain, shifter);
        // scheduler.setDefaultCommand(logger, logger);

        autoInit = new ParallelCommandGroup(
            new InstantCommand(climberPneumatics::extendClimb),
            new InstantCommand(climberPneumatics::retractBuddyClimb),
            new InstantCommand(shifter::lowGear)
        );

        //things that happen when the robot is inishlided
        teleInit = new ParallelCommandGroup(
            new InstantCommand(climberPneumatics::extendClimb),
            new InstantCommand(climberPneumatics::retractBuddyClimb),
            new InstantCommand(shifter::highGear)
        );
    }

    private void setDefaultCommands() {
        //drive by controler
        drivetrain.setDefaultCommand(new ArcadeDrive(
            drivetrain, 
            shifter, 
            controller1.rightTrigger,
            controller1.leftTrigger, 
            controller1.leftX
            ));

            //makes intake arm go back to limit when not on limit
        intakeArm.setDefaultCommand(new IntakeArmDefault(intakeArm));

    }

    private void configureButtonBindings() {
        // Launchpad bindings

        //climb
        launchpad.missileA.whenPressed(new ClimbSequence(leftArm, rightArm, climberPneumatics, launchpad.axisA,
        launchpad.axisB, joystick.axisY, launchpad.missileA, launchpad.bigLEDGreen, launchpad.bigLEDRed));

        launchpad.missileB.whenPressed(new InstantCommand(climberPneumatics::extendBuddyClimb));

        launchpad.buttonA.whenPressed(
            new InstantCommand(
                climberPneumatics::toggleClimb
            )
        );
        launchpad.buttonA.booleanSupplierBind(climberPneumatics::getClimbState);

        //moes
        launchpad.buttonB.whileActiveContinuous(new ClimberArmMO(rightArm, joystick.axisY), false);
        launchpad.buttonB.pressBind();

        launchpad.buttonC.whileActiveContinuous(new ClimberArmMO(leftArm, joystick.axisY), false);
        launchpad.buttonC.pressBind();

        launchpad.buttonD.pressBind();

        launchpad.buttonE.whileActiveContinuous(new ConveyorMO(joystick, conveyor, joystick.axisY), false);
        launchpad.buttonE.pressBind();

        launchpad.buttonF.whileActiveContinuous(new IntakeArmMO(joystick, intakeArm, joystick.axisY, joystick.trigger, joystick.button3), false);
        launchpad.buttonF.pressBind();

        //intake arm
        launchpad.buttonG.whenPressed(new SetLoad(intakeArm), false);
        launchpad.buttonG.booleanSupplierBind(intakeArm::isLoading);

        launchpad.buttonH.whenPressed(new SetDown(intakeArm), false);
        launchpad.buttonH.booleanSupplierBind(intakeArm::isDown);

        launchpad.buttonI.whenPressed(new SetUp(intakeArm), false);
        launchpad.buttonI.booleanSupplierBind(intakeArm::isUp);

        launchpad.buttonA.toggleWhenPressed(new StartEndCommand(
          intakeArm::closeLock,
          intakeArm::openLock,
          intakeArm
        ), true);
        launchpad.buttonA.toggleBind(); 

        // Controller bindings for intake
        controller1.buttonX.whenPressed(new SetUp(intakeArm), false);
        controller1.buttonA.whenPressed(new SetDown(intakeArm), false);
        controller1.buttonB.whenPressed(new SetLoad(intakeArm), false);

        //shifting
        controller1.leftBumper.toggleWhenPressed(new StartEndCommand(
            () -> {
                shifter.lowGear();
                launchpad.bigLEDRed.set(true);
                launchpad.bigLEDGreen.set(false);
            }, () -> {
                shifter.highGear();
                launchpad.bigLEDRed.set(false);
                launchpad.bigLEDGreen.set(true);
            }, shifter
        ));

    }

    /**
     * runs when robot is inited to telyop
     */
    public void teleopInit() {
        //inishlises robot
        scheduler.schedule(teleInit);
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return new SequentialCommandGroup(
            autoInit,
            new EncoderDrive(drivetrain, 50)
        );
    }
}
