package org.usfirst.frc.team1626.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.wpi.first.wpilibj.Utility;

public class DriverInput
{
	private static final long serialVersionUID = 1L;
	private long timeOffset;
	private static long recordStart;
	private List<Object> inputs;
	private static HashMap<String, Integer> inputNames=new HashMap<String, Integer>();
	
	public static void nameInput(String name)
	{
		Integer nextIdx=inputNames.size();
		inputNames.put(name, nextIdx);
	}
	
	public String toString()
	{
		StringBuffer str=new StringBuffer();
		str.append(timeOffset);
		for (Object o: inputs)
		{
			str.append(';');
			str.append(o.toString());
		}
		return str.toString();
	}
	
	public DriverInput(Object... in)
	{
		inputs=new ArrayList<Object>();
		for (Object obj : in)
		{
			inputs.add(obj);
		}
		long FPGAtime=Utility.getFPGATime();
		timeOffset=FPGAtime-recordStart;
//		System.out.println("Driver input offset is " + timeOffset + " = " + FPGAtime + " - " + recordStart);
	}
	
	public Object getInput(String name)
	{
		return inputs.get(inputNames.get(name));
	}
	
	public Object getInput(int idx)
	{
		return inputs.get(idx);
	}
	
	public long getTimeOffset()
	{
		return timeOffset;
	}
	
	public DriverInput setTimeOffset(long to)
	{
		timeOffset=to;
		return this;
	}
	
	public static void setRecordTime()
	{
		recordStart=Utility.getFPGATime();
		System.out.println("recordStart is " + recordStart);
	}
}
