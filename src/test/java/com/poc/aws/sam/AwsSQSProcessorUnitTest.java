package com.poc.aws.sam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aws.sam.poc.AwsSQSProcessor;

@RunWith(MockitoJUnitRunner.class)
public class AwsSQSProcessorUnitTest {
	
	
	static final Logger log = LoggerFactory.getLogger(AwsSQSProcessorUnitTest.class);
	
	AwsSQSProcessor awsSQSProcessor;
	
	

	@Test
	public void testInputMessageProcessing() {
		
		String processedText = AwsSQSProcessor
				.processJsonAndGetCombinations("{\r\n" + "	\"input\":\r\n" + "	[\r\n" + "		\"A\",\r\n"
						+ "		\"B\",\r\n" + "		\"C\",\r\n" + "		\"D\"\r\n" + "	]\r\n" + "}", log);
		 
		String expectedOuput = "{\"response\":[\"A\",\"B\",\"C\",\"D\",\"AB\",\"AC\",\"AD\",\"BC\",\"BD\",\"CD\",\"ABC\",\"ABD\",\"ACD\",\"BCD\",\"ABCD\"]}";
		Assert.assertEquals(expectedOuput, processedText);
	}
	
	@Test
	public void testSingleInputMessageProcessing() {
		String processedText = AwsSQSProcessor
				.processJsonAndGetCombinations("{\r\n" + "	\"input\":\r\n" + "	[\r\n" + "		\"A\"\r\n" + "	]\r\n" + "}", log);
		String expectedOuput = "{\"response\":[\"A\"]}";
		Assert.assertEquals(expectedOuput, processedText);
	}
	
	
	@Test
	public void testEmptyInputMessageProcessing() {
		String processedText = AwsSQSProcessor
				.processJsonAndGetCombinations("{\r\n" + "	\"input\":\r\n" + "	[]\r\n" + "}", log);
		Assert.assertEquals(null, processedText);
		
	}
	
	

}
