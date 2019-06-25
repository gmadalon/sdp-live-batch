package br.com.druid.sdp.configuration;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	
	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			
			log.info("Execution completed, time: " + String.valueOf((jobExecution.getEndTime().getTime() - jobExecution.getCreateTime().getTime())/1000 ) + " seconds");
			System.exit(0);
		}
	}
}
