package com.pasimann.producer.dataobjects;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ApplicationData {

    @Id
    @GeneratedValue
    private String id;

    private String data;
    private Date date;

    public ApplicationData() { }

    public ApplicationData(String data) {
        this.data = data;
    }

    public ApplicationData(String data, Date date) {
        this.data = data;
        this.date = date;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return this.data;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

	@Override
	public String toString() {
		return "ApplicationData [id=" + id + ", data=" + data + ", date=" + date.toString() + "]";
	}
}