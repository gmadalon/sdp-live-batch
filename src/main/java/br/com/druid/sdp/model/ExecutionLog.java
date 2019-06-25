package br.com.druid.sdp.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExecutionLog {
	
	
	private String externalCoId;
	
	private ExecutionLogEvent event;
	
	private LocalDateTime logDate;



}
