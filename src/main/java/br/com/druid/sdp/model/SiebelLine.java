package br.com.druid.sdp.model;

import lombok.Data;

@Data
public class SiebelLine {
	
	String indicator;
	String contract;
	String service;
	String description;
	String action;
	String updateDate;
	String module;
	String reason;
	String serviceItemPrice;

}
