package org.usfirst.frc.team1626.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.TalonSRX;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;

/*
 * Talon VII :: Robot
 * 
 * @author Rohan Mishra & Team 1626 Falcon Robotics
 * @version 1.0.0
 * 
 */

public class Robot extends IterativeRobot {
	
	private TalonSRX frontLeft;
	private TalonSRX frontRight;
	private TalonSRX backLeft;
	private TalonSRX backRight;
	
	private RobotDrive drive;
	
	private XboxController driverController;
	private XboxController shooterController;
	
	private Talon pickUpTalon;
	private Talon shooterOneTalon;
	private Talon shooterTwoTalon;
	private Talon winchTalon;
	
	private DoubleSolenoid gearShifter;
	boolean highGear = true;
	
	@Override
	public void robotInit() {		
		frontLeft          = new TalonSRX(0);
		frontRight         = new TalonSRX(1);
		backLeft           = new TalonSRX(2);
		backRight          = new TalonSRX(3);
		
		drive              = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		driverController   = new XboxController(1);
		
		pickUpTalon        = new Talon(4);
		shooterOneTalon    = new Talon(5);
		shooterTwoTalon    = new Talon(6);
		winchTalon         = new Talon(7);
		
		gearShifter        = new DoubleSolenoid(0, 1);
	}
	
	@Override
	public void autonomousInit() {
	}

	@Override
	public void autonomousPeriodic() {
	}

	@Override
	public void teleopPeriodic() {
		double leftAxisValue = driverController.getRawAxis(2);
		double rightAxisValue = driverController.getRawAxis(5);
		drive.tankDrive(leftAxisValue, rightAxisValue);
		
		// TODO - make sure solenoid values are correct (rn kFoward shifts to low gear)
		if (driverController.getStickButton() == true && highGear == true) {
			gearShifter.set(DoubleSolenoid.Value.kForward);
			highGear = false;
		} else if (driverController.getStickButton() == true && highGear == false) {
			gearShifter.set(DoubleSolenoid.Value.kReverse);
			highGear = true;
		}
	}

	@Override
	public void testPeriodic() {
	}
}

