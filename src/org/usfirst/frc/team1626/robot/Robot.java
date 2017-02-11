package org.usfirst.frc.team1626.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.TalonSRX;

import java.lang.reflect.InvocationTargetException;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;

import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Talon VII :: Robotics
 *  - Tankdrive with CANTalons
 *  - Shooter
 *  
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
	
	private Talon pickUpOneTalon;
	private Talon pickUpTwoTalon;
	private Talon shooterOneTalon;
	private Talon shooterTwoTalon;
	private Talon winchTalon;
	
	private PowerDistributionPanel pdp;
	
	private DoubleSolenoid gearShifter; 
	boolean highGear = true;
	
	int autoLoopCounter;
	ActionRecorder actions;
		
	@Override
	public void robotInit() {
		// TODO - Convert to CANTalon code
		frontLeft          = new TalonSRX(0);
		frontRight         = new TalonSRX(1);
		backLeft           = new TalonSRX(2);
		backRight          = new TalonSRX(3);
		
		drive              = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		xbox               = new XboxController(1);
		
		pickUpOneTalon     = new Talon(4);
		pickUpTwoTalon     = new Talon(6);
		shooterOneTalon    = new Talon(5);
		shooterTwoTalon    = new Talon(3);
		winchTalon         = new Talon(7);
		
		pdp                = new PowerDistributionPanel(0);
		
		gearShifter        = new DoubleSolenoid(0, 1);
		
		actions 		   = new ActionRecorder();
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(xbox, 1).
			setDownButton(xbox, 2).
			setRecordButton(xbox, 3);
		DriverInput.nameInput("Operator-X-Button");
		DriverInput.nameInput("Operator-Y-Button");
		DriverInput.nameInput("Operator-A-Button");
		DriverInput.nameInput("Operator-B-Button");
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
		
		try {
		actions.input(new DriverInput()
				.withInput("Operator-X-Button", xbox.getXButton())
				.withInput("Operator-Y-Button", xbox.getYButton())
				.withInput("Operator-A-Button", xbox.getAButton())
				.withInput("Operator-B-Button", xbox.getBButton()));
		
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
		
		if (xbox.getBumper() == true) {
			pickUpOneTalon.set(-.99);
		} else if (xbox.getBumper() == true) {
			pickUpOneTalon.set(.99);
		} else {
			pickUpOneTalon.set(0);
		}
		
		// TODO - make sure solenoid values are correct (rn kFoward shifts to low gear)
		if (xbox.getStickButton() == true && highGear == true) {
			gearShifter.set(DoubleSolenoid.Value.kForward);
			highGear = false;
		} else if (xbox.getStickButton() == true && highGear == false) {
			gearShifter.set(DoubleSolenoid.Value.kReverse);
			highGear = true;
		}
		
		SmartDashboard.putNumber("Voltage", pdp.getVoltage());
		// RoboRIO Brownout triggers @ 6.8V
		while (pdp.getVoltage() <= 7.0) {
			xbox.setRumble(RumbleType.kLeftRumble, 1.0);
			xbox.setRumble(RumbleType.kRightRumble, 1.0);
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
		System.out.println("Operating with: <" + input.toString() + ">");
		
		if (input.getButton("Operator-X-Button") == true) {
			shooterOneTalon.set(.99);
			shooterTwoTalon.set(.99);
		} else if (input.getButton("Operator-Y-Button") == true) {
			shooterOneTalon.set(-.99);
			shooterTwoTalon.set(-.99);
		} else {
			shooterOneTalon.set(0);
			shooterTwoTalon.set(0);
		}

		if (input.getButton("Operator-A-Button") == true) {
			pickUpOneTalon.set(.35);
			pickUpTwoTalon.set(-.35);
		} else if (input.getButton("Operator-B-Button") == true) {
			pickUpOneTalon.set(-.35);
			pickUpTwoTalon.set(.35);
		} else {
			pickUpOneTalon.set(0);
			pickUpTwoTalon.set(0);
		}
	}
	
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}

