package br.com.druid.sdp.configuration;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;

public class SiebelFlatFile implements FlatFileHeaderCallback, FlatFileFooterCallback {

	private static AtomicLong processedLines = new AtomicLong(0L);
	
	private static final String COLUMN_DELIMETER = "|";

	
	public static void addProcessedLines() {
		processedLines.incrementAndGet();
	}
	
	public static String createFileName() {
		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
        String formatDateTime = LocalDateTime.now().format(formatter);

		String fileName = "hist_serv_siebel_" +formatDateTime + ".txt";
		
		return fileName;
		
	}
	
	public static String getColumnDelimeter() {
		return COLUMN_DELIMETER;
	}	
	@Override
	public void writeHeader(Writer writer) throws IOException {
		
		processedLines.set(0L);
		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatDateTime = LocalDateTime.now().format(formatter);

		String header = "01" + COLUMN_DELIMETER + formatDateTime;
		writer.write(header);	
		
	}

	@Override
	public void writeFooter(Writer writer) throws IOException {

		String footer = "03" + COLUMN_DELIMETER + processedLines.get();
		writer.write(footer);	

	}

}
