 package com.aws.sam.poc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;

public class AwsSQSProcessor {
	
	public static final String QUEUE_NAME="CombinationProcessQueue";
	
	/**
	 * 
	 * @param S3data
	 * @param log
	 */
	public static void processQueue(String S3data, Logger log) {
	
		log.info("AwsSQSProcessor started!!");
		
		try { 
			AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
			
			//Send message into the queue
			log.info("Sending Messages to Queue is : "+ QUEUE_NAME);
			sqsClient.sendMessage(new SendMessageRequest(QUEUE_NAME, S3data));
			
			//Receive Messages from Queue
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME);
			List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
			log.info("Receiving Messages from Queue :" + QUEUE_NAME);
			
			messages.stream().forEach(msg -> {
				String messageId = msg.getMessageId();
				String messageQueue = msg.getBody();
				
				List<String> messageCombinationList = new ArrayList<String>();
				messageCombinationList = processJsonAndGetCombinations(messageQueue,log);
				
				//Delete Message from Queue 
				log.info("Delete Message from Queue : "+ QUEUE_NAME);
				
				String messageReceiptHandle = messages.get(0).getReceiptHandle();
				sqsClient.deleteMessage(new DeleteMessageRequest(QUEUE_NAME, messageReceiptHandle));
				
				//Insert the processed output into Dynamodb table
				com.aws.sam.poc.AwsDynamoDbProcessor.process(messageId, messageCombinationList, log);
			});
			
		}catch (AmazonSQSException amazonSQSException) {
			log.error("Amazon Service exception in AwsSQSProcessor : "+ amazonSQSException.getMessage());
			log.error("Status Code in AwsSQSProcessor : "+ amazonSQSException.getStatusCode()); 
			log.error("Error Code in AwsSQSProcessor : "+ amazonSQSException.getErrorCode());
		} catch (Exception e) {
			log.error("Exception while executing the AwsSQSProcessor : "+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param messageQueue
	 * @param log
	 * @return
	 */
	public static List<String> processJsonAndGetCombinations(String messageQueue, Logger log) {
		List<String> messageCombinationList;
		log.info("The Message from queue is : "+ messageQueue);
		
		StringBuilder builder = new StringBuilder();
		
		JSONObject jObj = new JSONObject(messageQueue);
		JSONArray jsonArray = jObj.getJSONArray("input");
		
		jsonArray.forEach((val)->{builder.append(val);});
		
		String messageToBeProcessed = builder.toString();
		
		if (messageToBeProcessed==null || messageToBeProcessed.isEmpty()) {
			log.info("Character length is ZERO to process");
			return null;
		}
		messageCombinationList = getCombinationsOfInputString(messageToBeProcessed);
						
	    log.info("Processed Message is : "+ messageCombinationList);
		return messageCombinationList;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	protected static List<String> getCombinationsOfInputString(String text) {
	    List<List<String>> results = new ArrayList<List<String>>() ;
		
	    IntStream.range(0, text.length()+1)
        .forEach( i ->
			results.add(Stream.ofAll(text.toCharArray())
					.map(Character::toUpperCase)
			        .combinations(i)
			        .map(Traversable::mkString)
			        .collect(Collectors.toList())));
	    
		return results.stream().flatMap(i -> i.stream().filter(s -> (s != null && s.length() > 0))).collect(Collectors.toList());  
	}
}
