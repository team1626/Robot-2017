package org.usfirst.frc.team1626.robot;

import java.lang.reflect.InvocationTargetException;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.AnalogInput;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;

import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * 
 * Talon VII :: Falcon Robotics Team 1626
 *  - Tankdrive
 *  - Xbox Controller
 *  - Shooter
 *  - Using the winch to climb
 *  - Ball pick up
 *  - Autonomous Echo code
 *  - Brownout protection
 *  
 * @author Rohan Mishra & Team 1626 Falcon Robotics
 * @version 1.0.0
 * 
 */

public class Robot extends IterativeRobot {
	private PowerDistributionPanel pdp;
	
	private CANTalon frontLeft;
	private CANTalon frontRight;
	private CANTalon backLeft;
	private CANTalon backRight;
	
	private RobotDrive drive;

	private XboxController xbox;
	private Joystick driveLeft;
	private Joystick driveRight;
	
	private Talon pickUpTalonOne;
	private Talon pickUpTalonTwo;
	private Talon shooterTalonOne;
	private Talon shooterTalonTwo;
	private Talon winchTalon;
	
	private DoubleSolenoid gearShifter;
	private boolean highGear = true;
	
	private AnalogInput pressureSensor;
	
	int autoLoopCounter;
	ActionRecorder actions;
		
	@Override
	public void robotInit() {
		pdp                = new PowerDistributionPanel(0);
		
		frontLeft          = new CANTalon(3);
		frontRight         = new CANTalon(10);
		backLeft           = new CANTalon(11);
		backRight          = new CANTalon(1);
		
		drive              = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		driveLeft          = new Joystick(0);
		driveRight		   = new Joystick(1);
		
		xbox               = new XboxController(2);
		
		pickUpTalonOne     = new Talon(4);
		pickUpTalonTwo     = new Talon(6);
		shooterTalonOne    = new Talon(5);
		shooterTalonTwo    = new Talon(3);
		winchTalon         = new Talon(7);
		
		gearShifter        = new DoubleSolenoid(0, 1);
		
		pressureSensor     = new AnalogInput(0);
		
		actions 		   = new ActionRecorder();
		
		// When robot turns on it is in low gear, this sets it into high gear
		gearShifter.set(DoubleSolenoid.Value.kReverse);
		
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(xbox, 1).
			setDownButton(xbox, 2).
			setRecordButton(xbox, 3);
		// TODO - Add Gear-Shifter input
		DriverInput.nameInput("Driver-Left");
		DriverInput.nameInput("Driver-Right");
		DriverInput.nameInput("Driver-Left-Trigger");
		DriverInput.nameInput("Driver-Right-Trigger");
		DriverInput.nameInput("Operator-Right-Trigger");
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
		try {
			if (actions != null) {
//				actions.playback();
				actions.longPlayback(this, -1);
			} else {
				Timer.delay(0.010);
			}
		} catch (Exception e) {
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
		double leftAxisValue = driveLeft.getRawAxis(1);
		double rightAxisValue = driveRight.getRawAxis(1);
		drive.tankDrive(leftAxisValue, rightAxisValue);
		
		if (xbox.getTrigger(Hand.kRight)) {
			shooterTalonOne.set(99);
			shooterTalonTwo.set(99);
		} else {
			shooterTalonOne.set(0);
			shooterTalonTwo.set(0);
		}
		
		if (xbox.getAButton()) {
			pickUpTalonOne.set(99);
			pickUpTalonTwo.set(99);
		} else {
			pickUpTalonOne.set(0);
			pickUpTalonTwo.set(0);
		}
		
		if (xbox.getBButton()) {
			winchTalon.set(99);
		} else {
			winchTalon.set(0);
		}
		
		// TODO - make sure solenoid values are correct (rn kFoward shifts to low gear)		
		if ((driveLeft.getRawButton(1) == true || driveRight.getRawButton(1) == true) && highGear == true) {
			gearShifter.set(DoubleSolenoid.Value.kForward);
			highGear = false;
		} else if ((driveLeft.getRawButton(1) == true || driveRight.getRawButton(1) == true) && highGear == false) {
			gearShifter.set(DoubleSolenoid.Value.kReverse);
			highGear = true;
		}
		
		// given vout, pressure = 250(vout/vcc) - 25
		// vss is assumed to be 5.0
		double pressure = (250.0 * (pressureSensor.getVoltage() / 5.0)) - 25;
		SmartDashboard.putNumber("Pressure", pressure);
		
		SmartDashboard.putNumber("Voltage", pdp.getVoltage());
		// RoboRIO Brownout triggers @ 6.8V		
		if (Timer.getMatchTime() >= 20.0) {
			while (pdp.getVoltage() <= 7.2) {
				xbox.setRumble(RumbleType.kLeftRumble, 1.0);
				xbox.setRumble(RumbleType.kRightRumble, 1.0);
				
				if (pdp.getVoltage() > 7.2) {
					// TODO - Reset certain components
					
					break;
				}
			}
		}
		
		try {
			actions.input(new DriverInput()
				.withInput("Driver-Left", driveLeft.getRawAxis(1))
				.withInput("Driver-Right", driveRight.getRawAxis(1))
				.withInput("Driver-Left-Trigger", driveLeft.getRawButton(1))
				.withInput("Driver-Right-Trigger", driveRight.getRawButton(1))
				.withInput("Operator-Right-Trigger", xbox.getTrigger(Hand.kRight))
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
		
		if (input.getTrigger("Operator-Right-Trigger") == true) {
			shooterTalonOne.set(.99);
			shooterTalonTwo.set(.99);
		} else {
			shooterTalonOne.set(0);
			shooterTalonTwo.set(0);
		}

		if (input.getButton("Operator-A-Button") == true) {
			pickUpTalonOne.set(.35);
			pickUpTalonTwo.set(-.35);
		} else {
			pickUpTalonOne.set(0);
			pickUpTalonTwo.set(0);
		}
		
		if (input.getButton("Operator-B-Button")) {
			winchTalon.set(99);
		} else {
			winchTalon.set(0);
		}
	}
	
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}

