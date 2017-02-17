package org.usfirst.frc.team1626.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.TalonSRX;

import java.lang.reflect.InvocationTargetException;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;

import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * 
 * Talon VII :: Robotics
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
	
	private PowerDistributionPanel pdp;
	
	private DoubleSolenoid gearShifter; 
	private boolean highGear = true;
	
	int autoLoopCounter;
	ActionRecorder actions;
		
	@Override
	public void robotInit() {
		// TODO - Convert to CANTalon code
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
		
		pdp                = new PowerDistributionPanel(0);
		
		gearShifter        = new DoubleSolenoid(0, 1);
		
		actions 		   = new ActionRecorder();
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(xbox, 1).
			setDownButton(xbox, 2).
			setRecordButton(xbox, 3);
		// TODO - Add Gear-Shifter input
		DriverInput.nameInput("Driver-Left");
		DriverInput.nameInput("Driver-Right");
		DriverInput.nameInput("Driver-Trigger");
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
		
		double leftAxisValue = xbox.getRawAxis(2);
		double rightAxisValue = xbox.getRawAxis(5);
		drive.tankDrive(leftAxisValue, rightAxisValue);
		
		if (xbox.getBumper()) {
			winchTalon.set(-.99);
		} else if (xbox.getBumper() == true) {
			winchTalon.set(.99);
		} else {
			winchTalon.set(0);
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
		if (Timer.getMatchTime() >= 20.0) {
			while (pdp.getVoltage() <= 7.2) {
				xbox.setRumble(RumbleType.kLeftRumble, 1.0);
				xbox.setRumble(RumbleType.kRightRumble, 1.0);
				
				if (pdp.getVoltage() > 7.2) {
					// Reset certain components
					
					break;
				}
			}
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
			shooterTalonOne.set(.99);
			shooterTalonTwo.set(.99);
		} else if (input.getButton("Operator-Y-Button") == true) {
			shooterTalonOne.set(-.99);
			shooterTalonTwo.set(-.99);
		} else {
			shooterTalonOne.set(0);
			shooterTalonTwo.set(0);
		}

		if (input.getButton("Operator-A-Button") == true) {
			pickUpTalonOne.set(.35);
			pickUpTalonTwo.set(-.35);
		} else if (input.getButton("Operator-B-Button") == true) {
			pickUpTalonOne.set(-.35);
			pickUpTalonTwo.set(.35);
		} else {
			pickUpTalonOne.set(0);
			pickUpTalonTwo.set(0);
		}
	}
	
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}

