package com.njuguna.dailyselfie.model;

import com.njuguna.dailyselfie.data.LocationAuditedSynchedModel;

public class Reminder extends LocationAuditedSynchedModel {

	private Long initTime;

	@Override
	public String toString() {
		return "Reminder{" +
				"initTime=" + initTime +
				'}';
	}

	public Long getInitTime() {
		return initTime;
	}

	public void setInitTime(Long initTime) {
		this.initTime = initTime;
	}
}
