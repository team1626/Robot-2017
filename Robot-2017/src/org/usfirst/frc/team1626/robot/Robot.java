package org.usfirst.frc.team1626.robot;

import java.lang.reflect.InvocationTargetException;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;

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
 *  
 * @author Rohan Mishra & Team 1626 Falcon Robotics
 * @version 1.0.0
 * 
 */

public class Robot extends IterativeRobot {
	private PowerDistributionPanel pdp;
	
	private CANTalon frontLeftTalon;
	private CANTalon frontRightTalon;
	private CANTalon backLeftTalon;
	private CANTalon backRightTalon;
	
	private Talon winchTalon;
	private Talon pickUpTalon;
	private Talon agitatorTalon;
	private CANTalon shooterTalonOneTop;
	private CANTalon shooterTalonOneBottom;
	private CANTalon shooterTalonTwoTop;
	private CANTalon shooterTalonTwoBottom;
	
	private DoubleSolenoid gearShifter;
	private DoubleSolenoid driveTrainShifter;
	Toggle highGear;
	
	private AnalogInput pressureSensor;
	
	private RobotDrive drive;
	private Joystick driverLeft;
	private Joystick driverRight;
	
	private XboxController xbox;
	
	int autoLoopCounter;
	ActionRecorder actions;
		
	@Override
	public void robotInit() {
		pdp               		 = new PowerDistributionPanel(0);
		
		frontLeftTalon         	 = new CANTalon(3);
		frontRightTalon        	 = new CANTalon(10);
		backLeftTalon       	 = new CANTalon(11);
		backRightTalon     		 = new CANTalon(1);
		
		winchTalon               = new Talon(0);
		pickUpTalon    		 	 = new Talon(1);
		agitatorTalon			 = new Talon(2);
		shooterTalonOneTop       = new CANTalon(4);
		shooterTalonOneBottom    = new CANTalon(6);
		shooterTalonTwoTop       = new CANTalon(2);
		shooterTalonTwoBottom    = new CANTalon(5);
		
		gearShifter			     = new DoubleSolenoid(6, 7);
		driveTrainShifter        = new DoubleSolenoid(4, 5);
		highGear				 = new Toggle();
		
		pressureSensor    		 = new AnalogInput(0);
		
		drive            		 = new RobotDrive(frontLeftTalon, backLeftTalon, frontRightTalon, backRightTalon);
		driverLeft         		 = new Joystick(0);
		driverRight		  		 = new Joystick(1);
		
		xbox               		 = new XboxController(2);
		
		actions 		   		 = new ActionRecorder();
		
		shooterTalonOneTop.setInverted(true);
		// Robot initially in low gear, this sets it into high gear
		driveTrainShifter.set(DoubleSolenoid.Value.kReverse);
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(xbox, 1).
			setDownButton(xbox, 2).
			setRecordButton(xbox, 3);
		DriverInput.nameInput("Driver-Left");
		DriverInput.nameInput("Driver-Right");
		DriverInput.nameInput("Driver-Left-Trigger");
		DriverInput.nameInput("Driver-Right-Trigger");
		DriverInput.nameInput("Operator-Left-Stick");
		DriverInput.nameInput("Operator-Right-Stick");
		DriverInput.nameInput("Operator-Left-Trigger");
		DriverInput.nameInput("Operator-Right-Trigger");
		DriverInput.nameInput("Operator-X-Button");
		DriverInput.nameInput("Operator-A-Button");
		DriverInput.nameInput("Operator-B-Button");
		DriverInput.nameInput("Operator-Y-Button");
		DriverInput.nameInput("Operator-Start-Button");
		DriverInput.nameInput("Operator-Back-Button");
	} 
	
	@Override
	public void robotPeriodic() {		
		// given vout, pressure = 250(vout/vcc) - 25
		// vcc is assumed to be 5.0
		double pressure = (250.0 * (pressureSensor.getVoltage() / 5.0)) - 25;
		SmartDashboard.putString("DB/String 4", String.format("%.1f", pressure));
		SmartDashboard.putNumber("PDP Voltage", pdp.getVoltage());
		
		// RoboRIO Brownout triggers @ 6.8V		
		if (Timer.getMatchTime() >= 15.0) {
			while (pdp.getVoltage() <= 7.2) {
				xbox.setRumble(RumbleType.kLeftRumble, 1.0);
				xbox.setRumble(RumbleType.kRightRumble, 1.0);
						
				if (pdp.getVoltage() > 7.2) {
					// TODO - Reset certain components
							
					break;
				}
			}
		}
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
				.withInput("Driver-Left", driverLeft.getRawAxis(1))
				.withInput("Driver-Right", driverRight.getRawAxis(1))
				.withInput("Driver-Left-Trigger", driverLeft.getRawButton(1))
				.withInput("Driver-Right-Trigger", driverRight.getRawButton(1))
				.withInput("Operator-Left-Trigger", xbox.getTrigger(Hand.kLeft))
				.withInput("Operator-Right-Trigger", xbox.getTrigger(Hand.kRight))
				.withInput("Operator-X-Button", xbox.getXButton())
				.withInput("Operator-Y-Button", xbox.getYButton())
				.withInput("Operator-A-Button", xbox.getAButton())
				.withInput("Operator-B-Button", xbox.getBButton())
				.withInput("Operator-Start-Button", xbox.getStartButton())
				.withInput("Operator-Back-Button", xbox.getBackButton()));
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
		
		double leftJoystickAxis = input.getAxis("Driver-Left");
		double rightJoystickAxis = input.getAxis("Driver-Right");
		drive.tankDrive(leftJoystickAxis, rightJoystickAxis);
		
		boolean shift = (input.getButton("Driver-Right-Trigger") || input.getButton("Driver-Left-Trigger"));
		highGear.input(shift);
		System.out.println("Gear State: " + highGear.getState() + "Button: " + shift);
		if (highGear.getState()) {
			driveTrainShifter.set(DoubleSolenoid.Value.kForward);
		} else {
			driveTrainShifter.set(DoubleSolenoid.Value.kReverse);
		}
		
		if (input.getButton("Operator-X-Button")) {
			shooterTalonOneTop.set(.99);
			shooterTalonOneBottom.set(.99);
			shooterTalonTwoTop.set(.99);
			shooterTalonTwoBottom.set(.99);
			agitatorTalon.set(.99);
		} else if (input.getButton("Operator-Y-Button")) {
			shooterTalonOneTop.set(-.99);
			shooterTalonOneBottom.set(-.99);
			shooterTalonTwoTop.set(-.99);
			shooterTalonTwoBottom.set(-.99);
			agitatorTalon.set(-.99);
		} else {
			shooterTalonOneTop.set(0);
			shooterTalonOneBottom.set(0);
			shooterTalonTwoTop.set(0);
			shooterTalonTwoBottom.set(0);
			agitatorTalon.set(0);
		}
		
		if (input.getButton("Operator-A-Button")) {
			pickUpTalon.set(.99);
		} else if (input.getButton("Operator-B-Button")) {
			pickUpTalon.set(-.99);
		} else {
			pickUpTalon.set(0);
		}
		
		if (input.getButton("Operator-Start-Button")) {
			winchTalon.set(.99);
		} else if (input.getButton("Operator-Back-Button")) {
			winchTalon.set(-.99);
		} else {
			winchTalon.set(0);
		}
		
		if (input.getButton("Operator-Left-Axis")) {
			System.out.println(xbox.getRawAxis(2));
		}
	}
	
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}

