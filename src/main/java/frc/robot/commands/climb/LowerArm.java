package frc.robot.commands.climb;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ClimberArm;

public class LowerArm extends CommandBase {

	private ClimberArm arm;
	private int distance;

	public LowerArm(ClimberArm arm, int distance) {
		this.arm = arm;
		this.distance = distance;

		addRequirements(arm);
	}

	@Override
	public void initialize() {
		arm.setEncoderPosition(0);
		arm.setPower(-1);
	}

	@Override
	public void end(boolean interrupted) {
		arm.stop();
	}

	@Override
	public boolean isFinished() {
		return arm.getEncoderPosition() <= distance;
	}
}