package com.aws.sam.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;

public class AWSLambdaS3FunctionHandler implements RequestHandler<S3Event, String>  {
	
	static final Logger log = LoggerFactory.getLogger(AWSLambdaS3FunctionHandler.class);
	

	@Override
	public String handleRequest(S3Event event, Context context) {
		
		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
			
			String bucketName = "aws-sam-poc-999";
			
			event.getRecords().stream().forEach(i -> {
				String file = i.getS3().getObject().getKey();
				log.info("File received is : " + file + ", and bucket info is : " + bucketName);
				String s3Object = s3Client.getObjectAsString(bucketName, file);
				log.info(file + " File content is : " + s3Object);
				com.aws.sam.poc.AwsSQSProcessor.processQueue(s3Object.toString(), log); });
			
//			fileName = event.getRecords().get(0).getS3().getObject().getKey();
//			
//			log.info("File received is : " + fileName + ", and bucket info is : " + bucketName);
//			
//			String s3Object = s3Client.getObjectAsString(bucketName, fileName);
//			
//			log.info(fileName + " File content is : " + s3Object);
//			
//			com.aws.sam.poc.AwsSQSProcessor.processQueue(s3Object.toString(), log);
			
		} catch (AmazonS3Exception amS3Exception) {
			log.error("Amazon S3 exception in Lambda Function Handler : " + amS3Exception.getMessage());
			log.error("Status Code in Lambda Function Handler : " + amS3Exception.getStatusCode());
			log.error("Error Code in Lambda Function Handler : " + amS3Exception.getErrorCode());
		}  catch (Exception e) {
			log.error("Exception in Lambda Function Handler : " +e.getMessage());
		}
		
		return "Success!!";
	}

	
}
