package org.usfirst.frc.team1626.robot;

public class Toggle {
	
	boolean toggleState;
	boolean prevButtonState;
	
	public Toggle() {
		toggleState=false;
		prevButtonState=false;
	}
	
	public boolean input(boolean state) {
		if (state && ! prevButtonState) {
			toggle();
		}
		prevButtonState = state;
		return toggleState;
	}
	
	private boolean toggle() {
		if (toggleState) {
			toggleState=false;
		} else {
			toggleState = true;
		}
		
		
		return toggleState;
	}
	
	public boolean getState() {
		return toggleState;
	}

}
