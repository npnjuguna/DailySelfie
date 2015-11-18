package com.njuguna.dailyselfie.server.controller;

@SuppressWarnings("serial")
public class FeatureNotImplementedException extends RuntimeException {
	public FeatureNotImplementedException() {
		super("Feature not implented yet!");
    }
}