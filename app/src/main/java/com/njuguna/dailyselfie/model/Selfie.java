package com.njuguna.dailyselfie.model;

import android.graphics.Bitmap;

import com.njuguna.dailyselfie.data.LocationAuditedSynchedModel;

public class Selfie extends LocationAuditedSynchedModel {

	private String Caption;
	private String originalPath;
	private Bitmap originalBitmap;
	private String thumbnailPath;
	private Bitmap thumbnailBitmap;
	private String savedPath;
	private Bitmap savedBitmap;

	public Selfie() {
	}

	public Selfie(String guid) {
        this.guid = guid;
    }

	@Override
	public String toString() {
		return "Selfie{" +
				"Caption='" + Caption + '\'' +
				", originalPath='" + originalPath + '\'' +
				", thumbnailPath='" + thumbnailPath + '\'' +
				", savedPath='" + savedPath + '\'' +
				'}';
	}

	public String getCaption() {
		return Caption;
	}

	public void setCaption(String caption) {
		Caption = caption;
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}

	public String getThumbnailPath() {
		return thumbnailPath;
	}

	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

	public Bitmap getThumbnailBitmap() {
		return thumbnailBitmap;
	}

	public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
		this.thumbnailBitmap = thumbnailBitmap;
	}

	public Bitmap getOriginalBitmap() {
		return originalBitmap;
	}

	public void setOriginalBitmap(Bitmap originalBitmap) {
		this.originalBitmap = originalBitmap;
	}

	public Bitmap getSavedBitmap() {
		return savedBitmap;
	}

	public void setSavedBitmap(Bitmap savedBitmap) {
		this.savedBitmap = savedBitmap;
	}

	public String getSavedPath() {
		return savedPath;
	}

	public void setSavedPath(String savedPath) {
		this.savedPath = savedPath;
	}
}
