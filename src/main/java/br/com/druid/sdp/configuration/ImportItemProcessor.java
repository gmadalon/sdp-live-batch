package br.com.druid.sdp.configuration;

import java.time.format.DateTimeFormatter;

import org.springframework.batch.item.ItemProcessor;

import br.com.druid.sdp.model.ExecutionLog;
import br.com.druid.sdp.model.ExecutionLogEvent;
import br.com.druid.sdp.model.SiebelLine;

public class ImportItemProcessor implements ItemProcessor<ExecutionLog, SiebelLine> {
   
    

    @Override
    public SiebelLine process(final ExecutionLog  executionLog) throws Exception {
        SiebelLine  siebelLine = new SiebelLine();
        siebelLine.setContract(executionLog.getExternalCoId());
        
        if(ExecutionLogEvent.SUBSCRIPTION_CREATION.equals(executionLog.getEvent())) {
        	siebelLine.setAction("a");
        } else if(ExecutionLogEvent.SUBSCRIPTION_DELETION.equals(executionLog.getEvent())) {
        	siebelLine.setAction("d");
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatUpdateDate = executionLog.getLogDate().format(formatter);

        siebelLine.setUpdateDate(formatUpdateDate);
        SiebelFlatFile.addProcessedLines();
        return siebelLine;
    }

}
