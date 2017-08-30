package com.riseofcat.lib;

public class DegreesAngle extends Angle {
public DegreesAngle(double degrees) {
	super(degrees/180*Math.PI);
}
}
