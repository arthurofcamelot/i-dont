package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.logging.Logger;
import frc.robot.logging.LoggerRelations;
import frc.robot.utilities.Functions;
import frc.robot.utilities.Ports;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

/**
 * Subsystem to control the drivetrain of the robot
 */
public class Drivetrain implements Subsystem, Logger {

    private static final double
    P = .05,
    I = 0,//.00004,
    D = 0;//.00001;

    //data for logger
    private double rightMotorPower = 0, leftMotorPower = 0, rightMotorTarget = 0, leftMotorTarget = 0;

    // left motors
    private CANSparkMax left = new CANSparkMax(Ports.LEFT_DRIVE_MAIN, MotorType.kBrushless);
    private CANSparkMax leftMiddle = new CANSparkMax(Ports.LEFT_DRIVE_0, MotorType.kBrushless);
    private CANSparkMax leftBack = new CANSparkMax(Ports.LEFT_DRIVE_1, MotorType.kBrushless);

    // right motors
    private CANSparkMax right = new CANSparkMax(Ports.RIGHT_DRIVE_MAIN, MotorType.kBrushless);
    private CANSparkMax rightMiddle = new CANSparkMax(Ports.RIGHT_DRIVE_0, MotorType.kBrushless);
    private CANSparkMax rightBack = new CANSparkMax(Ports.RIGHT_DRIVE_1, MotorType.kBrushless);

    // pid controllers
    private CANPIDController leftPID = left.getPIDController();
    private CANPIDController rightPID = right.getPIDController();

    // encoders
    private CANEncoder leftEncoder = left.getEncoder();
    private CANEncoder rightEncoder = right.getEncoder();

    private double oldOpenRampRate; // the previous ramp rate sent to the motors
    private double oldClosedRampRate; // the previous ramp rate sent to the motors

    // pid config
    private double 
    FEED_FWD = 0, 
    // change later, just so a problem doesn't break my walls
    OUTPUT_MIN = -1, 
    OUTPUT_MAX = 1;

    public Drivetrain() {
        // tells other two motors to follow the first
        leftMiddle.follow(left);
        leftBack.follow(left);

        rightMiddle.follow(right);
        rightBack.follow(right);

        // inverts right side
        left.setInverted(true);
        right.setInverted(false);

        // sets pid values
        leftEncoder.setPosition(0);
        rightEncoder.setPosition(0);

        leftPID.setP(P);
        leftPID.setI(I);
        leftPID.setD(D);
        leftPID.setOutputRange(OUTPUT_MIN, OUTPUT_MAX);
        
        rightPID.setP(P);
        rightPID.setI(I);
        rightPID.setD(D);
        rightPID.setOutputRange(OUTPUT_MIN, OUTPUT_MAX);

        leftPID.setOutputRange(-0.25, 0.25);
        rightPID.setOutputRange(-0.25, 0.25);

        left.disableVoltageCompensation();
        right.disableVoltageCompensation();
        
        leftMiddle.disableVoltageCompensation();
        rightMiddle.disableVoltageCompensation();

        leftBack.disableVoltageCompensation();
        rightBack.disableVoltageCompensation();

        setClosedRampRate(0);
    }

    /**
     * Sets the power of the left side of the drivetrain
     * 
     * @param power -1 - 1
     */
    public void setLeftMotorPower(double power) {
        power = Functions.clampDouble(power, 1.0, -1.0);
        leftMotorPower = power;
        left.set(power);
    }

    /**
     * Sets the power of the right side of the drivetrain
     * 
     * @param power -1 - 1
     */
    public void setRightMotorPower(double power) {
        power = Functions.clampDouble(power, 1.0, -1.0);
        rightMotorPower = power;
        right.set(power);
    }

    /**
     * Sets the target position of the left side of the drivetrain
     * 
     * @param position the target position in terms of motor rotations
     */
    public void setLeftMotorTarget(double position) {
        leftMotorPower = 2;
        leftMotorTarget = position;
        leftPID.setReference(position, ControlType.kPosition);
    }

    /**
     * Sets the target position of the right side of the drivetrain
     * 
     * @param position the target position in terms of motor rotations
     */
    public void setRightMotorTarget(double position) {
        rightMotorPower = 2;
        rightMotorTarget = position;
        rightPID.setReference(position, ControlType.kPosition);
    }

    /**
     * The position you want the left side to register when it is in the position it
     * is currently in
     * 
     * @param position the position for the encoder to register in rotations
     */
    public void setLeftEncoder(int position) {
        leftEncoder.setPosition(position);
    }

    /**
     * The position you want the right side to register when it is in the position
     * it is currently in
     * 
     * @param position the position for the encoder to register in rotations
     */
    public void setRightEncoder(double position) {
        rightEncoder.setPosition(position);
    }

    /**
     * Returns the current position of right side of the drivetrain
     * 
     * @return position of motor in rotations
     */
    public double getRightEncoderPosition() {
        return rightEncoder.getPosition();
    }

    /**
     * Returns the current position of right side of the drivetrain
     * 
     * @return position of motor in rotations
     */
    public double getLeftEncoderPosition() {
        return leftEncoder.getPosition();
    }

    /**
     * Sets the rate at witch the motors ramp up and down in open loop control mode
     * 
     * @param rate time in seconds to go from 0 to full power
     */
    public void setOpenRampRate(double rate) {
        // checks against old ramp rate to prevent unnecessary ramp-rate sets at they take
        // lots of cpu time
        if (rate != oldOpenRampRate) {
            left.setOpenLoopRampRate(rate);
            right.setOpenLoopRampRate(rate);
            oldOpenRampRate = rate;
        }
    }

    /**
     * Sets the rate at which the motors ramp up and down in closed loop control
     * mode
     * 
     * @param rate time in seconds to go from 0 to full power
     */
    public void setClosedRampRate(double rate) {
        // checks against old ramp rate to prevent unnecessary ramp-rate sets at they take
        // lots of cpu time
        if (rate != oldClosedRampRate) {
            left.setClosedLoopRampRate(rate);
            right.setClosedLoopRampRate(rate);
            oldClosedRampRate = rate;
        }
    }

    /**
     * Stops the motors
     */
    public void stop() {
        left.stopMotor();
        right.stopMotor();
    }

    /**
     * Makes the robot turn by a power
     * 
     * @param power power of the motors
     */
    public void turn(double power) {
        setLeftMotorPower(-power);
        setRightMotorPower(power);
    }

    @Override
    public void periodic() {

        /*
        System.out.println(left.getBusVoltage());
        System.out.println(left.getAppliedOutput());
        System.out.println("------------------------");
        System.out.println(right.getBusVoltage());
        System.out.println(right.getAppliedOutput());
        System.out.println("+++++++++++++++++++++++++++++++");
        */
    }

    @Override
    public double[] getValues(double[] values) {
        values[LoggerRelations.LEFT_MOTOR_POWER.value] = leftMotorPower;
        values[LoggerRelations.RIGHT_MOTOR_POWER.value] = rightMotorPower;
        values[LoggerRelations.LEFT_MOTOR_TARGET.value] = leftMotorTarget;
        values[LoggerRelations.RIGHT_MOTOR_TARGET.value] = rightMotorTarget;
        values[LoggerRelations.LEFT_MOTOR_POSITION.value] = getLeftEncoderPosition();
        values[LoggerRelations.RIGHT_MOTOR_POSITION.value] = getRightEncoderPosition();
        
        return values;
    }
}