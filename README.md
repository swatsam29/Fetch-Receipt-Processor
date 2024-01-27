Receipt Processor

Utilized Java (Spring Boot), Docker, and Postman API.

Step 1: To build the project in Docker, use the following command:

docker build -t springreceiptprocessor.jar .

Step 2: To run the project, execute the following command:

docker run -p 9090:8080 springreceiptprocessor.jar

API calls:
To test the microservice, use the following path:

Post Method: "/receipts/generate-id"

Get Method: "/receipts/{id}/points"
