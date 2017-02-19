package org.usfirst.frc.team1626.robot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ActionRecorder {
	private boolean recording = false;
	private boolean recordingReady = false;
	private long playbackStart;
	
	private List <DriverInput> driverInputs;
	private Iterator <DriverInput> playbackIterator;
	
	private Object playbackObject;
	private Method playbackMethod;
	
	private StateButton upButton;
	private StateButton downButton;
	private StateButton recordButton;
	
	private List <File> autoFileList;
	private int autoFileIndex;
	private File fileToRecord = null; 
	
	// For timing accuracy measurements
	private long Sx     = 0;
	private long Sx2    = 0;
	private long Sxy    = 0;
	private long Sy     = 0;
	private long Sy2    = 0;
	private long n      = 0;
	
	public class StateButton {
		private int buttonNumber;
		private XboxController controller;
		private boolean prevState;
		
		public StateButton(XboxController stick, int button) {
			controller = stick;
			buttonNumber = button;
			prevState = false;
		}
		
		public boolean getState() {
			boolean rv  = false;
			boolean currState = controller.getRawButton(buttonNumber);
			rv  = currState && !prevState;
			prevState = currState;
			return rv;			
		}
	}
	
	public ActionRecorder() {
		recording = false;
		recordingReady = false;
	}

	@SuppressWarnings("rawtypes")
	private Method lookUpMethod(Object obj, String methodName, Class... args) {
		Method method = null;
		try {
			method = obj.getClass().getMethod(methodName,  args);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return method;
	}

	@SuppressWarnings("rawtypes")
	public ActionRecorder setMethod(Object obj, String methodName,  Class... args) {
		Method method;
		if ((method = lookUpMethod(obj, methodName, args)) != null) {
			playbackObject = obj;
			playbackMethod = method;
		}
		return this;
	}

	public ActionRecorder setController(XboxController stick) {
		return this;
	}

	public ActionRecorder setUpButton(XboxController stick, int up) {
		upButton = new StateButton(stick, up);
		return this;
	}

	public ActionRecorder setDownButton(XboxController stick, int down) {
		downButton = new StateButton(stick, down);
		return this;
	}
	
	public ActionRecorder setRecordButton(XboxController stick, int rec) {
		recordButton = new StateButton(stick, rec);
		return this;
	}

	public void startRecording() {
		recording = true;
		recordingReady = false;
		SmartDashboard.putBoolean("Auto/Recording", true);
		SmartDashboard.putBoolean("DB/LED 0", true);
	}
	
	public boolean isRecording() {
		return recording;
	}

	public void stopRecording() {
		recording = false;
		SmartDashboard.putBoolean("Auto/Recording", false);
		SmartDashboard.putBoolean("DB/LED 0", false);
	}

	public void toggleRecording() {
		if (isRecording()) {
			stopRecording();
		} else {
			startRecording();
		}
	}
	
	public void displayName() {
		SmartDashboard.putString("Auto/FileName", autoFileList.get(autoFileIndex).getName());
		SmartDashboard.putString("DB/String 0", autoFileList.get(autoFileIndex).getName());
	}
	
	private int getAutoFileList() {
		autoFileList = new ArrayList<File>();
		File autoDir = new File("/home/lvuser/auto");
		System.out.println("Auto Root is: " + autoDir.getAbsolutePath());
		File[] autoLs = autoDir.listFiles();
		System.out.println("Containing " + autoLs.length + " files");

		int newIdx = -1;

		if (autoLs != null) {
			for (File f : autoLs) {
				if (f.isFile()) {
					System.out.println("File<" + f.getAbsolutePath() + ">");
					String fName=f.getName();
					
					if (fName.matches("new[0-9]+\\.csv")) {
						int dotPos = fName.indexOf('.', 3);
						System.out.println("dot pos is " + dotPos);
						
						if (dotPos > 3) {
							String idx = fName.substring(3, dotPos);
							int fNum=Integer.parseInt(idx);
							System.out.println("num<" + idx + ">=" + fNum);

							if (fNum >= newIdx) {
								newIdx=fNum+1;
								System.out.println("new index is " + newIdx);
							}
						}
					}
				}
				
				autoFileList.add(f);
			}
		}
		
		return newIdx;
	}
	
	private void writeDriverInputs() {
		try {
			BufferedWriter outFile= new BufferedWriter(new FileWriter(fileToRecord));
			
			for (DriverInput input: driverInputs) {
				outFile.write(input.toString());
				outFile.write("\n");
			}
			
			outFile.close();
		} catch (IOException e) {
			System.out.println(fileToRecord.getAbsolutePath() + ": " + e.toString());
			e.printStackTrace(System.out);
		}
	}

	/*
	 * m=(n*Sxy - Sx*Sy/(n*Sx2-(Sx)**2)
	 * b=(Sy-b*Sx)/n
	 * s.d. = sqrt((Sx2/n)-(Sx/n)**2)
	 */

	public void disabledInit() {
		System.out.println("Entering disabledInit");
		
		System.out.println("n=" + n +
				" Sx=" + Sx + " Sx2=" + Sx2 + " Sxy=" + Sxy + " Sy=" + Sy + " Sy2=" + Sy2);
				
		double m = ((double)(n*Sxy - Sx*Sy))/(((double)(n*Sx2))-Math.pow((double)Sx,2));
		double b = ((double)(Sy-m*Sx))/((double)n);
		double mean = ((double)Sy)/((double)n);
		double sd = Math.sqrt(((double)Sy2/(double)n)-Math.pow(mean,2));

		SmartDashboard.putNumber("Auto/Timing/Slope", m);
		SmartDashboard.putNumber("Auto/Timing/Intercept", b);
		SmartDashboard.putNumber("Auto/Timing/Standard Deviation",  sd);
		SmartDashboard.putNumber("Auto/Timing/Mean",  mean);
		
		if (isRecording()) {
			if ((driverInputs != null) && (driverInputs.size() > 0)) {
				writeDriverInputs();
			}
		}
		
		stopRecording();
		
		int newIdx=getAutoFileList();

		autoFileIndex=0;
		autoFileList.add(new File("/home/lvuser/auto", "new" + String.format("%03d.csv", newIdx)));
		displayName();
	}

	public void disabledPeriodic() {
		if ((recordButton != null) && recordButton.getState()) {
			toggleRecording();
		}
		
		if ((upButton != null) && upButton.getState()) {
			if (autoFileIndex < (autoFileList.size()-1)) {
				autoFileIndex++;
				displayName();
			}
		}
		
		if ((downButton != null) && downButton.getState()) {
			if (autoFileIndex > 0) {
				autoFileIndex--;
				displayName();
			}
		}

		playbackIterator=null;
	}

	public void input(DriverInput drIn) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (isRecording()) {
			if (!recordingReady) {
				driverInputs = new ArrayList<DriverInput>();
				recordingReady = true;
			}
			
			driverInputs.add(drIn);
		}
		
		playbackMethod.invoke(playbackObject,  drIn);
	}
	
	public void longPlayback(RobotBase robot, int nCycles) {
		while (((nCycles < 0) || nCycles > 0) && (robot.isAutonomous() && robot.isEnabled())) {
			playback();
			nCycles--;
		}

	}
	
	public void playback() {
		if ((driverInputs == null) || (driverInputs.size() == 0)) {
			System.out.println("No driver inputs to playback");
			Timer.delay(0.050);
			return;
		}
		
		if (playbackIterator == null) {
			System.out.println("Creating Iterator for " + driverInputs.size() + " inputs");
			playbackIterator=driverInputs.iterator();
			playbackStart=Utility.getFPGATime();
		}

		if (playbackIterator.hasNext()) {
			DriverInput input=playbackIterator.next();
			
//			System.out.println("input time offset is " + input.getTimeOffset());

			double delayForPlayback=((double)(playbackStart+input.getTimeOffset() - Utility.getFPGATime()))/1000000.0;
//			System.out.println("Delay before input is " + delayForPlayback);

			if (delayForPlayback > 0) {
				Timer.delay(delayForPlayback);
			}
			
			long expectedTime=playbackStart+input.getTimeOffset();
			long timeError=Utility.getFPGATime() - expectedTime;
			
			Sx += expectedTime;
			Sx2 += (expectedTime*expectedTime);
			Sxy += (expectedTime*timeError);
			Sy2 += (timeError*timeError);
			Sy += timeError;
			
			n++;

			try {
				playbackMethod.invoke(playbackObject,  input);
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
		} else {
			Timer.delay(0.010);
		}
	}
	
	private void readAutoFile(File autoFile) {
		BufferedReader inFile = null;
		try {
			inFile = new BufferedReader(new FileReader(autoFile));
			driverInputs = new ArrayList<DriverInput>();
			
			String line;
			
			while ((line = inFile.readLine()) != null) {
//				System.out.println("Line was <" + line + ">");
				String[] tokens = line.split(";");
				int timeOffset = Integer.parseInt(tokens[0]);
				Object[] drIn = new Object[tokens.length-1];
				
				for (int i=1; i < tokens.length; i++) {
					if (tokens[i].equalsIgnoreCase("true") || tokens[i].equalsIgnoreCase("false")) {
						drIn[i-1] = new Boolean(tokens[i]);
					} else if (tokens[i].equalsIgnoreCase("null")) {
						drIn[i-1] = null;
					} else {
						drIn[i-1] = new Double(tokens[i]);
					}
				}
				
				DriverInput input = new DriverInput(drIn);
				input.setTimeOffset(timeOffset);
				driverInputs.add(input);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void teleopInit() {
		if (isRecording()) {
			SmartDashboard.getString("DB/String 0", "new_auto.csv");
			fileToRecord = autoFileList.get(autoFileIndex);
		}
	}

	public void autonomousInit() {
//		System.out.println("Entering autonomous init with " + autoFileList.get(autoFileIndex).getAbsoluteFile());
		File autoFile = autoFileList.get(autoFileIndex);
		if (autoFile.canRead()) {
//			System.out.println("Reading <" + autoFile.getName() + ">");
			readAutoFile(autoFile);
		}
		if (driverInputs == null) {
			System.out.println("No Auto File");
		} else {
			System.out.println("Auto File has " + driverInputs.size() + " elements");
			Sx = 0;
			Sx2 = 0;
			Sxy = 0;
			Sy = 0;
			Sy2 = 0;
			n = 0;

		}
	}
}