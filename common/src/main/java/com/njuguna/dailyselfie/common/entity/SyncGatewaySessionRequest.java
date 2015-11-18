package com.njuguna.dailyselfie.common.entity;

public class SyncGatewaySessionRequest {
	
	private String name;
	private Long ttl;
	
	public SyncGatewaySessionRequest() {
	}

	public SyncGatewaySessionRequest(String name, Long ttl) {
		this.name = name;
		this.ttl = ttl;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getTtl() {
		return ttl;
	}
	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}

}
