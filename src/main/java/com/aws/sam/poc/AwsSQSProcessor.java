 package com.aws.sam.poc;

import java.util.ArrayList;
import java.util.List;

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

public class AwsSQSProcessor {
	
	public static final String QUEUE_NAME="CombinationProcessQueue";
	
	
	public static ArrayList<String> processQueue(String S3data, Logger log) {
	
		log.info("AwsSQSProcessor started!!");
		
		ArrayList<String> messageCombinationList = new ArrayList<String>();
		
		try { 
			AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
			
			//Send a message
			log.info("Sending Message to Queue is : "+ QUEUE_NAME);
			sqsClient.sendMessage(new SendMessageRequest(QUEUE_NAME, S3data));
			
			//Receive Messages
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(QUEUE_NAME);
			List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
			log.info("Receiving Messages from Queue :" + QUEUE_NAME);
			
			
			String messageId = "";
			String messageQueue = "";
			for (Message message:messages) {
				messageQueue = message.getBody();
				messageId = message.getMessageId();
			}
			
			messageCombinationList = processJsonAndGetCombinations(messageQueue,log);
			
			//Delete Message from Queue :
			
			log.info("Delete Message from Queue : "+ QUEUE_NAME);
			
			String messageReceiptHandle = messages.get(0).getReceiptHandle();
			sqsClient.deleteMessage(new DeleteMessageRequest(QUEUE_NAME, messageReceiptHandle));
			
			com.aws.sam.poc.AwsDynamoDbProcessor.process(messageId, messageCombinationList, log);
			
		}catch (AmazonSQSException amazonSQSException) {
			log.error("Amazon Service exception in AwsSQSProcessor : "+ amazonSQSException.getMessage());
			log.error("Status Code in AwsSQSProcessor : "+ amazonSQSException.getStatusCode()); 
			log.error("Error Code in AwsSQSProcessor : "+ amazonSQSException.getErrorCode());
		} catch (Exception e) {
			log.error("Exception while executing the AwsSQSProcessor : "+ e.getMessage());
		}
		return messageCombinationList;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<String> processJsonAndGetCombinations(String messageQueue, Logger log) {
		ArrayList<String> messageCombinationList;
		log.info("The Message from queue is : "+ messageQueue);
		
		StringBuilder builder = new StringBuilder();
		
		JSONObject jObj = new JSONObject(messageQueue);
		JSONArray jsonArray = jObj.getJSONArray("input");
		
		jsonArray.forEach((val)->{builder.append(val);});
		
		String messageToBeProcessed = builder.toString();
		if (messageToBeProcessed.isBlank()) {
			log.info("Character length is ZERO to process");
			return null;
		}
		messageCombinationList = getCombinationsOfInputString(messageToBeProcessed, log);
						
	    log.info("Processed Message is : "+ messageCombinationList);
		return messageCombinationList;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	protected static ArrayList<String> getCombinationsOfInputString(String text,  Logger log) {
	    ArrayList<String> results = new ArrayList<String>();
	   
	    for (int i = 0; i < text.length(); i++) {
	        // Record size as the list will change
	        int resultsLength = results.size();
	        for (int j = 0; j < resultsLength; j++) {
	            results.add(text.charAt(i) + results.get(j));
	        }
	        results.add(Character.toString(text.charAt(i)));
	        
	    }
	    return results;
	}
}
