package com.aws.sam.poc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AWSLambdaS3FunctionHandler implements RequestHandler<S3Event, String> {
	
	static final Logger log = LoggerFactory.getLogger(AWSLambdaS3FunctionHandler.class);


	@Override
	public String handleRequest(S3Event event, Context context) {
		
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
		
		String bucketName = event.getRecords().get(0).getS3().getBucket().getName();
		String fileName = event.getRecords().get(0).getS3().getObject().getKey();
		
		StringBuffer displaySQSInputStream = new StringBuffer();
		
		log.info("File received is : " + fileName + ", and bucket info is : " + bucketName);
		
		processS3EventContent(context, s3Client, bucketName, fileName, displaySQSInputStream);
		
		return "S3Event notified for Object : " + fileName;
	}

	/**
	 * 
	 * @param context
	 * @param s3Client
	 * @param bucketName
	 * @param fileName
	 * @param displaySQSInputStream
	 */
	private void processS3EventContent(Context context, AmazonS3 s3Client, String bucketName, String fileName,
			StringBuffer displaySQSInputStream) {
		try(S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, fileName))) {
			
			readFileContent(context, fileName, displaySQSInputStream, s3Object);
			
			log.info(fileName + " File content is : " + displaySQSInputStream);
			com.aws.sam.poc.AwsSQSProcessor.processQueue(displaySQSInputStream.toString(), log);
			
		} catch (IOException e) {
			log.error("Failed to get the file content to s3Object : "+fileName + e.getStackTrace());
		} catch (AmazonS3Exception amS3Exception) {
			log.error("Amazon S3 exception in Lambda Function Handler : " + amS3Exception.getMessage());
			log.error("Status Code in Lambda Function Handler : " + amS3Exception.getStatusCode());
			context.getLogger().log("Error Code in Lambda Function Handler : " + amS3Exception.getErrorCode());
		}  catch (Exception e) {
			log.error("Exception in Lambda Function Handler : " +e.getMessage());
		}
	}

	/**
	 * 
	 * @param context
	 * @param fileName
	 * @param displaySQSInputStream
	 * @param s3Object
	 */
	private void readFileContent(Context context, String fileName, StringBuffer displaySQSInputStream,
			S3Object s3Object) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				displaySQSInputStream.append(line + System.lineSeparator());
			}

		} catch (IOException e) {
			log.error("Failed to read the content of the file : "+fileName + e.getStackTrace());
		}
	}

}
