package br.com.druid.sdp.configuration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import br.com.druid.sdp.model.ExecutionLog;
import br.com.druid.sdp.model.SiebelLine;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value("${batch.maxthreads:1}")
	private int batchMaxthreads;

	@Value("${batch.outputDir:./data}")
	private String outputDir;


	@Bean
	public JdbcCursorItemReader<ExecutionLog> reader(DataSource dataSource) {

		JdbcCursorItemReader<ExecutionLog> databaseReader = new JdbcCursorItemReader<ExecutionLog>();

		databaseReader.setDataSource(dataSource);
		databaseReader.setSql(
				" SELECT EXTERNAL_CO_ID, EVENT, LOG_DATE FROM EXECUTION_LOG WHERE LOG_DATE >= TRUNC(SYSDATE) -1 AND IS_OK = 1 AND EVENT IN ( 'SUBSCRIPTION_CREATION' , 'SUBSCRIPTION_DELETION' )  ");
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExecutionLog.class));

		return databaseReader;

	}

	@Bean
	public ImportItemProcessor processor() {
		return new ImportItemProcessor();
	}

	@Bean
	public FlatFileItemWriter<SiebelLine> writer() {
		FlatFileItemWriter<SiebelLine> writer = new FlatFileItemWriter<SiebelLine>();
		writer.setResource(new FileSystemResource(SiebelFlatFile.createFileName()));

		SiebelFlatFile siebelFlatFile = new SiebelFlatFile();
		writer.setHeaderCallback(siebelFlatFile);
		writer.setFooterCallback(siebelFlatFile);

		DelimitedLineAggregator<SiebelLine> csvLineAggregator = new DelimitedLineAggregator<SiebelLine>();
		csvLineAggregator.setDelimiter(SiebelFlatFile.getColumnDelimeter());
		BeanWrapperFieldExtractor<SiebelLine> fieldExtractor = new BeanWrapperFieldExtractor<SiebelLine>();
		fieldExtractor.setNames(new String[] { "indicator", "contract", "service", "description", "action",
				"updateDate", "module", "reason", "serviceItemPrice" });
		csvLineAggregator.setFieldExtractor(fieldExtractor);

		writer.setLineAggregator(csvLineAggregator);
		return writer;
	}
	// end::readerwriterprocessor[]

	// tag::listener[]

	@Bean
	public JobExecutionListener listener() {
		return new JobCompletionNotificationListener();
	}

	// end::listener[]

	// tag::jobstep[]
	@Bean
	public Job siebelFileJob(DataSource dataSource) {
		return jobBuilderFactory.get("SiebelFileJob").incrementer(new RunIdIncrementer()).listener(listener())
				.flow(siebelFileStep(dataSource)).end().build();
	}

	@Bean
	public Step siebelFileStep(DataSource dataSource) {
		return stepBuilderFactory.get("SiebelFileStep").<ExecutionLog, SiebelLine>chunk(batchMaxthreads)
				.reader(reader(dataSource)).processor(processor()).writer(writer()).taskExecutor(taskExecutor())
				.throttleLimit(batchMaxthreads).build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(batchMaxthreads);
		taskExecutor.setCorePoolSize(batchMaxthreads);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

}
