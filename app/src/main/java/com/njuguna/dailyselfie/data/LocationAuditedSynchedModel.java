package com.njuguna.dailyselfie.data;

public abstract class LocationAuditedSynchedModel {

	/**
	 * Global Unique Identifier of the object. It is generated when the
	 * object is created
	 */
	protected String guid;
	private Long id;
	private Long createDate;
	private String createBy;
	private Double createLat;
	private Double createLong;
	private Long updateDate;
	private String updateBy;
	private Double updateLat;
	private Double updateLong;

	public LocationAuditedSynchedModel() {
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Double getCreateLat() {
		return createLat;
	}

	public void setCreateLat(Double createLat) {
		this.createLat = createLat;
	}

	public Double getCreateLong() {
		return createLong;
	}

	public void setCreateLong(Double createLong) {
		this.createLong = createLong;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Double getUpdateLat() {
		return updateLat;
	}

	public void setUpdateLat(Double updateLat) {
		this.updateLat = updateLat;
	}

	public Double getUpdateLong() {
		return updateLong;
	}

	public void setUpdateLong(Double updateLong) {
		this.updateLong = updateLong;
	}

	@Override
	public String toString() {
		return "LocationAuditedSynchedModel{" +
				"guid='" + guid + '\'' +
				", id=" + id +
				", createDate=" + createDate +
				", createBy='" + createBy + '\'' +
				", createLat=" + createLat +
				", createLong=" + createLong +
				", updateDate=" + updateDate +
				", updateBy='" + updateBy + '\'' +
				", updateLat=" + updateLat +
				", updateLong=" + updateLong +
				'}';
	}
}