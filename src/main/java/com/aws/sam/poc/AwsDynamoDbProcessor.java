package com.aws.sam.poc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

public class AwsDynamoDbProcessor {
	
	private static final String TABLE_NAME="Sequence";
	
	public static void process(String messageId, ArrayList<String> messageArrayList, Logger log) {
		
		log.info("DynamoDb processor started!!");
		log.info("message id is : "+messageId + "and arraylist is : "+messageArrayList);
		try {
			AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
				
			insertRecord(messageId, messageArrayList, log, dynamoDB);
		} catch (AmazonDynamoDBException amDynamoDBException) {
			log.error("Amazon DynamoDb exception in AwsDynamoDbProcessor : "+ amDynamoDBException.getMessage());
			log.error("Status Code in AwsDynamoDbProcessor : "+ amDynamoDBException.getStatusCode()); 
			log.error("Error Code in AwsDynamoDbProcessor : "+ amDynamoDBException.getErrorCode());
		} catch (Exception e) {
			log.error("Exception while executing the AwsDynamoDbProcessor : "+ e.getMessage());
			
		}
	}

	private static void insertRecord(String messageId, ArrayList<String> messageArrayList, Logger log,
			AmazonDynamoDB dynamoDB) {
		
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("MessageId", new AttributeValue(messageId));
		item.put("ResultSet", new AttributeValue(messageArrayList));
		
		
		PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
		log.info("put item request is : "+putItemRequest.toString());
		PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
		
		log.info("Result is successfully stored in table : "+ TABLE_NAME);
	}

}
