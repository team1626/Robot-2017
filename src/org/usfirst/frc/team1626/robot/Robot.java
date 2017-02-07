package org.usfirst.frc.team1626.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.TalonSRX;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;

import java.lang.reflect.InvocationTargetException;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;

import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
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
	
	private XboxController xbox;
	
	private Talon pickUpTalon;
	private Talon shooterOneTalon;
	private Talon shooterTwoTalon;
	private Talon winchTalon;
	
	private DoubleSolenoid gearShifter;
	boolean highGear = true;
	
	int autoLoopCounter;
	ActionRecorder actions;
		
	@Override
	public void robotInit() {		
		frontLeft          = new TalonSRX(0);
		frontRight         = new TalonSRX(1);
		backLeft           = new TalonSRX(2);
		backRight          = new TalonSRX(3);
		
		drive              = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		xbox               = new XboxController(1);
		
		pickUpTalon        = new Talon(4);
		shooterOneTalon    = new Talon(5);
		shooterTwoTalon    = new Talon(6);
		winchTalon         = new Talon(7);
		
		gearShifter        = new DoubleSolenoid(0, 1);
		
		actions 		   = new ActionRecorder();
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(xbox, 1).
			setDownButton(xbox, 2).
			setRecordButton(xbox, 3);
		DriverInput.nameInput("LeftTriggerAxis");
		DriverInput.nameInput("RightTriggerAxis");
	}
	
	@Override
	public void autonomousInit() {
		autoLoopCounter = 0;
		actions.autonomousInit();
	}

	@Override
	public void autonomousPeriodic() {
		try
		{
			if (actions != null)
			{
//				actions.playback();
				actions.longPlayback(this, -1);
			} else
			{
				Timer.delay(0.010);
			}
		} catch (Exception e)
		{
			System.out.println("AP: " + e.toString());
		}
	}
	
	@Override
	public void teleopInit() {
		DriverInput.setRecordTime();
		actions.teleopInit();
	}

	@Override
	public void teleopPeriodic() {
		double leftAxisValue = xbox.getRawAxis(2);
		double rightAxisValue = xbox.getRawAxis(5);
		drive.tankDrive(leftAxisValue, rightAxisValue);
		
		// TODO - make sure solenoid values are correct (rn kFoward shifts to low gear)
		if (xbox.getStickButton() == true && highGear == true) {
			gearShifter.set(DoubleSolenoid.Value.kForward);
			highGear = false;
		} else if (xbox.getStickButton() == true && highGear == false) {
			gearShifter.set(DoubleSolenoid.Value.kReverse);
			highGear = true;
		}
		
		try {
		actions.input(new DriverInput(xbox.getRawAxis(2),
									  xbox.getRawAxis(5)));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void disabledInit() {
		actions.disabledInit();
	}

	@Override
	public void disabledPeriodic() {
		actions.disabledPeriodic();
	}
	
	public void robotOperation(DriverInput input) {

	}
	
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}

