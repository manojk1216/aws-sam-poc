# AWS SAM POC - Lambda, SQS, Dynamodb

## Table Contents
	1. Create Role from AWS Console
	2. Create executable package to upload it to lambda fn
	3. Login into AWS CLI
	4. Create S3 bucket
	5. Create Dynamodb table
	6. Create SQS Queue
	7. Create lambda fn and add S3 trigger
	8. Upload file to S3 bucket to test
	

## Login into AWS Console

## Goto IAM Section and create a Role with "lambda_execution_role" with the following policies attached to role in permissions section
 
	 AmazonSQSFullAccess
	 AmazonS3FullAccess
	 CloudWatchFullAccess
	 AmazonDynamoDBFullAccess
	 AWSLambda_FullAccess
 
 ### Copy the Role ARN, which will be used at the time of Lambda function creation
		Ex: arn:aws:iam::<<ACCOUNT_NUMBER>>:role/service-role/sqs_lambda_dynamo-role-h6a1z6y2

## Open command prompt and compile the project and create package with following maven command

	C:\Users\acer>mvn clean install package
	
	Note: This command creates  the executable jar file in the 'target' directory
	
## Now configure the aws-cli to configure and execute the application from command line
	
	C:\Users\acer>aws configure
	
	AWS Access Key ID [None]: AKIAWP6EFKUCMCK5UP4X
	AWS Secret Access Key [None]: +f2lOo7
	Default region name [None]: ap-south-1
	Default output format [None]:



## Create S3 bucket, with default permissions

	C:\Users\acer>aws s3 mb s3://aws-sam-poc-999
	
	output:
		make_bucket: aws-sam-poc-999 

NOTE: S3 Bucket will be created at global level so no need to specify the region


## Create Dynamodb table with primary partition key as "MessageId"


	C:\Users\acer>aws dynamodb create-table --table-name Sequence --attribute-definitions AttributeName=MessageId,AttributeType=S --key-schema AttributeName=MessageId,KeyType=HASH --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1
	
	output:
		{
			"TableDescription": {
				"AttributeDefinitions": [
					{
						"AttributeName": "MessageId",
						"AttributeType": "S"
					}
				],
				"TableName": "Sequence",
				"KeySchema": [
					{
						"AttributeName": "MessageId",
						"KeyType": "HASH"
					}
				],
				"TableStatus": "CREATING",
				"CreationDateTime": "2021-06-20T17:10:23.979000+05:30",
				"ProvisionedThroughput": {
					"NumberOfDecreasesToday": 0,
					"ReadCapacityUnits": 1,
					"WriteCapacityUnits": 1
				},
				"TableSizeBytes": 0,
				"ItemCount": 0,
				"TableArn": "arn:aws:dynamodb:ap-south-1:<<ACCOUNT_NUMBER>>:table/Sequence",
				"TableId": "57a4025a-1348-494b-a396-3afad28b4aca"
			}
		}



## Create SQS Queue with default properties

	C:\Users\acer>aws sqs create-queue --queue-name CombinationProcessQueue
	
	output:
		{
			"QueueUrl": "https://sqs.ap-south-1.amazonaws.com/<<ACCOUNT_NUMBER>>/CombinationProcessQueue"
		}


## Create a lambda function to S3 Event will be triggered upon uploading a input file in S3 Bucket which inturn send processed message to DynamoDB through SQS


	C:\Users\acer>aws lambda create-function --function-name lambda_s3_sqs_dynamo_function --zip-file fileb://sam-poc.zip --handler com.aws.sam.poc.AWSLambdaS3FunctionHandler::handleRequest --runtime java8 --role arn:aws:iam::<<ACCOUNT_NUMBER>>:role/service-role/sqs_lambda_dynamo-role-h6a1z6y2

	output:
		{
			"FunctionName": "lambda_s3_sqs_dynamo_function",
			"FunctionArn": "arn:aws:lambda:ap-south-1:<<ACCOUNT_NUMBER>>:function:lambda_s3_sqs_dynamo_function",
			"Runtime": "java8",
			"Role": "arn:aws:iam::<<ACCOUNT_NUMBER>>:role/service-role/sqs_lambda_dynamo-role-h6a1z6y2",
			"Handler": "com.aws.sam.poc.AWSLambdaS3FunctionHandler::handleRequest",
			"CodeSize": 8179921,
			"Description": "",
			"Timeout": 3,
			"MemorySize": 128,
			"LastModified": "2021-06-20T12:01:04.966+0000",
			"CodeSha256": "5UppCvdMDzNNB30v5+VFEMJyoA+xt+28qhetG6IfE7Y=",
			"Version": "$LATEST",
			"TracingConfig": {
				"Mode": "PassThrough"
			},
			"RevisionId": "8858c150-dd67-49cc-81cd-a89fa66905fd",
			"State": "Active",
			"LastUpdateStatus": "Successful",
			"PackageType": "Zip"
		}


		8.1 Set permissions to lambda function to set S3 as triggering event

			C:\Users\acer>aws lambda add-permission --function-name function:lambda_s3_sqs_dynamo_function --profile default  --statement-id Allo
			wToBeInvoked --action "lambda:InvokeFunction" --principal s3.amazonaws.com --source-arn "arn:aws:s3:::aws-sam-poc-999
			"  --source-account <<ACCOUNT_NUMBER>>
			{
				"Statement": "{\"Sid\":\"AllowToBeInvoked\",\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"s3.amazonaws.com\"},\"Action\":\"lambda:InvokeFunction\",\"Resource\":\"arn:aws:lambda:ap-south-1:<<ACCOUNT_NUMBER>>:function:lambda_s3_sqs_dynamo_function\",\"Condition\":{\"StringEquals\":{\"AWS:SourceAccount\":\"<<ACCOUNT_NUMBER>>\"},\"ArnLike\":{\"AWS:SourceArn\":\"arn:aws:s3:::aws-sam-poc-999\"}}}"
			}

		8.2 Add S3 trigger event to the lambda function

			C:\Users\acer>aws s3api put-bucket-notification-configuration --bucket aws-sam-poc-999 --notification-configuration file://trigger.json


			file content of  ->  trigger.json 

			{
			"LambdaFunctionConfigurations": [
				{
				  "Id": "s3eventtriggerslambda",
				  "LambdaFunctionArn": "arn:aws:lambda:ap-south-1:<<ACCOUNT_NUMBER>>:function:lambda_s3_sqs_dynamo_function",
				  "Events": ["s3:ObjectCreated:*"]
				}
			  ]
			}
			

## Upload the input test file to S3 bucket
	
		aws s3 sync "C:\Users\acer\test" s3://aws-asm-test
