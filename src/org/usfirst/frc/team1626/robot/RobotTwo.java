package org.usfirst.frc.team1626.robot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Joystick.AxisType;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTwo extends IterativeRobot {
	Servo bottomServo;
	Servo middleServo;
	Servo topServo;
	Joystick stick;
	int autoLoopCounter;
	ActionRecorder actions;



	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		bottomServo = new Servo(7);
		middleServo = new Servo(5);
		topServo = new Servo(3);
		stick = new Joystick(0);
		actions = new ActionRecorder();
		actions.setMethod(this, "robotOperation", DriverInput.class).
			setUpButton(stick, 1).
			setDownButton(stick, 3).
			setRecordButton(stick, 2);
		DriverInput.nameInput("X-Axis");
		DriverInput.nameInput("Y-Axis");
		DriverInput.nameInput("Z-Axis");
	}

	/**
	 * This function is run once each time the robot enters autonomous mode
	 */
	public void autonomousInit() {
		autoLoopCounter = 0;
		actions.autonomousInit();
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic()
	{
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

	/**
	 * This function is called once each time the robot enters tele-operated mode
	 */
	public void teleopInit()
	{
		DriverInput.setRecordTime();
		actions.teleopInit();
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		try {
			actions.input(new DriverInput(
					stick.getAxis(AxisType.kX),
					stick.getAxis(AxisType.kY),
					stick.getAxis(AxisType.kZ)));
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
	public void disabledInit()
	{
		actions.disabledInit();
	}

	@Override
	public void disabledPeriodic()
	{
		actions.disabledPeriodic();
	}

	public void robotOperation(DriverInput input)
	{
		bottomServo.setAngle(((Double)input.getInput("X-Axis")+1.0)*85.0);
		middleServo.setAngle(((Double)input.getInput("Y-Axis")+1.0)*85.0);
		topServo.setAngle(((Double)input.getInput("Z-Axis")+1.0)*85.0);

	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		LiveWindow.run();
	}

}
