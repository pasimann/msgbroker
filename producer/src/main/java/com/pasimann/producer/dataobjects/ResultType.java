package com.pasimann.producer.dataobjects;

import javax.persistence.Entity;

@Entity
public class ResultType {

    private String errorCode;
    private String status;
    private String uuid;

    public ResultType() { }

    public ResultType(String errorCode, String status, String uuid) {
        this.errorCode = errorCode;
        this.status = status;
        this.uuid = uuid;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getStatus() {
        return this.status;
    }

    public String getUuid() {
        return this.uuid;
    }

	@Override
	public String toString() {
		return "ResultType [errorCode=" + errorCode + ", status="
            + status + ", uuid=" + uuid + "]";
	}
}