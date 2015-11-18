package com.njuguna.dailyselfie.server.entity;

public class SyncGatewayDocumentResponse {
	
	private String id;

	private String rev;

	private Boolean ok;
	
	public SyncGatewayDocumentResponse() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(String rev) {
		this.rev = rev;
	}

	public Boolean isOk() {
		return ok;
	}

	public void setOk(Boolean ok) {
		this.ok = ok;
	}

	
}
