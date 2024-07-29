# Using AWS Lambda with Keycloak

In this repository I have given code to start up Keycloak with Postgresql and code for using AWS Lambda functions with Node.js and Java. 

## Install Amazon AWS CLI
 
Follow the guide in amazon aws documentation to amazon aws cli v2 if you want to deploy the AWS Lambda function to AWS Lambda: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html

## How to setup Docker AWS Lambda Node.js image

Follow the instructions given here: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html#nodejs-image-instructions

## Node_modules

If you get issues with node_modules. Go into lambda-node folder and delete node_modules folder and run following command to install npm packages:
```
cd lambda-node
npm install
```

## How to run Lambda code in Node.js

* In lambda-node folder make modifications in getSessions.mjs and terminateSessions.mjs by changing the following variables: (Note: You can use environment variables to set these variables but I have set default values wwhich I am using.)
    * BASE_URL to your keycloak url. As I am running lambda-node as a service along with keycloak-demo I am using http://keycloak_demo:8080 which is defined in docker-compose for service name for keycloak i.e. keycloak_demo. Also, as it is running on the same network the internal port 8080 will work not 8990 which is for accessing keycloak outside docker.
    * KEYCLOAK_CLIENT_ID should be the id of your client created in master realm. 
       * When creating this client select "Client authentication" option to on and select authorization as on as well. Deselect "Standard Flow" and "Direct Access Grants". Service accounts roles will be enabled automatically when you select client authentication.
        * Also in client settings go to Service account roles for your created client and assign the following roles to the client for both master and the realm you are going to get/terminate session for.
            * view-clients, manage-users, query-user, view-users
    * KEYCLOAK_CLIENT_SECRET - Go to master realm --> clients --> select the client you have created --> Credentials and copy the client secret from here.
    * KEYCLOAK_REALM - change it to the realm you want to query to get sessions for. This is the realm your users reside in. You can set it here or leave it as default and in your REST call to AWS lambda send in the **realm** paramter(Will be discussed shortly). 
* Also, right now I am using the same container to test getSessions and terminateSessions so you will need to modify package.json **"main"** value to **"getSessions.mjs"** if you want to test getting sessions and **"terminateSessions.mjs"**. 
* Also, you will need to go to Dockerfile in lambda-node folder and change the CMD command to the functionality you want to test. You should use **CMD [ "getSessions.handler" ]** for testing getSessions and **CMD [ "terminateSessions.handler" ]** for terminating sessions. Comment out the other script cmd command in dockerfile.
* Now just run the following command to run the docker container which runs Keycloak, DB and lambda-node service in the root of the project.
```
docker-compose up --build -d
```
* If you want to make major changes than you should run the following commands to first take down the containers and then rebuild them.
```
docker-compose down --remove-orphans
docker-compose up --build -d
```
* If you make changes to the node lambda folder and just want to redeploy the service for lambda-node then use the following commands which quickly takes down lambda-node service and recreates it with the latest changes in lambda-node folder.
```
docker-compose stop lambda-node
docker-compose up lambda-node --build -d
```

## Testing Node.js lambda code locally

If you have your docker environment running then you can test the lambda functions locally by following the instructions in previous section and then using either Postman or use Windows PowerShell. You can find instructions to test locally for linux/macOS by reading the section on (Optional) Test the image locally on https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html but I will post configuration for testing in Postman and Powershell below.

### Powershell
```
Invoke-WebRequest -Uri "http://localhost:9000/2015-03-31/functions/function/invocations" -Method Post -Body '{"username": "user", "realm": "Demo"}' -ContentType "application/json"
```
As our service is running on localhost port 9000 the url is given. We are giving input/payload to our Lambda Node code by using Body paramter and **"username"** value should be the user name of the user we are trying to get sessions for or termiante sessions for. **"realm"** is optional but is the realm in which user exists for which we want to get/terminate sessions for(If you don't give realm paramter in Body then the default value in mjs files for KEYCLOAK_REALM will be used.)

The request remains same for getSessions and terminateSessions you just have to change package.json, and Dockerfile in lambda-node and run the commands in previous section to rebuild the lambda-node service.

### Postman

* Create a new request in Postman
* Change HTTP method of request to **POST**
* Set the request url to **http://localhost:9000/2015-03-31/functions/function/invocations**
* Set **Content-Type** Header in Header tab to **application/json**
* Go to Body tab of the request, Change the body type to **raw** and select dropdown above the body to **JSON**.
* Add the following body. Realmname is optional. If you don't give it any realm paramter then it will default to KEYCLOAK_REALM in mjs files.
```
{
    "username": "user",
    "realm": "Demo"
}
```
* Send Request and you will response in JSON

## Node.JS Lambda Functions Responses

### For getSessions

* If there are sessions you will get response:
```
{"statusCode":200,"body":[{"id":"ec107b9d-734d-4690-9d95-8845aa417be8","username":"user","userId":"536f08e5-0f63-458d-b0b2-990735979a6b","ipAddress":"192.168.65.1","start":1722256373000,"lastAccess":1722256375000,"rememberMe":false,"clients":{"64486b68-2e40-4adc-9f31-eae5c3491501":"account-console"},"transientUser":false}]}
```
The body part will contain multiple sessions if user is logged in multiple places. Here only 1 session is found for the user.
* If user is not found then in "body" you will get message **No User Found**
* If user is not logged in then the body array will be empty

### For terminateSessions
* If there are sessions for user then it will log out the users and output **"Logout successful. Total sessions terminated: 2"** for example if 2 sessions found.
```
{"statusCode":200,"body":"Logout successful. Total sessions terminated: 2"}
```
* If user is not found then in "body" you will get message **No User Found**
* If no user sessions found for the user then you will get message **No Sessions Found**


## Deploying the Lambda Function to AWS Lambda For Node.js

Follow the instructions given in guide by aws here: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html#nodejs-image-instructions. In **Using an AWS base image for Node.js** section go to part **Deploying the image** and follow the instructions. I don't have a remote keycloak server running so I did not test deploying to AWS Lambda remotely but I tested the lambda functions locally through docker. 


# For Java

## Changing Environment Variables

In both App.java in src/main/com/keycloak/sessionsapp under getsessions and terminatesessions folder under lambda-java there are some changes that need to be made. (You will need to change 2 files both App.java in sessions folders)
* In handleRequest change serverUrl to your keycloak url.
* Optionally you can change realmName to the realm you want to query but if you want you can give this realmName when invoking the AWS lambda function through terminal, aws command or Postman by setting **realm** value in body.
* In lines **46-52** change clientId to your main master client id you created and client secret to your secret.
Other than the above changes no other change is needed but you need to look at the next section on how to compile the code to create classes and lib files.

## Compiling Java code

Go to the root of the folder i.e. lambda-java/getsessions or lambda-java/terminatesessions in terminal and run the following command(You need to have maven installed on your computer)
```
mvn compile dependency:copy-dependencies -DincludeScope=runtime
```
* After this is sucessful you will need to follow the instructions for deploying on docker to run the code.

## Deploying Java Changes to Docker

* If your docker containers for this propject are not running then run the command below to build java code:
```
docker-compose down --remove-orphans
docker-compose up --build -d
```
* If containers are running and you make changes in lambda-java folder src file then just run the following commands to recreate the lambda-java services:
For getsessions
```
docker-compose stop lambda-java-getsessions
docker-compose up lambda-java-getsessions --build -d
```
For terminatesessions
```
docker-compose stop lambda-java-terminatesessions
docker-compose up lambda-java-terminatesessions --build -d
```

## Testing the Java Lambda Function Locally Docker

Follow the above guide [Testing Node.js lambda code locally](#testing-nodejs-lambda-code-locally) to call the java service. The rest is the same just you need to change the port in the request url to **9100** for **getsessions** and **9200** for **terminatesessions**. Otherwise the calling mechanism and the response are the same. 

# Reources

* Articles explaining how this code was created: 
    * https://medium.com/@jawadrashid/accessing-keycloak-api-using-node-js-and-aws-lambda-part-1-5782437afaf2
    * TODO: LINK TO PART HERE
* Github code for this tutorial: https://github.com/jawadrashid2011/aws-lambda-final
* My previous tutorial on how to setup implement Keycloak SPI Event Listener using docker: https://medium.com/@jawadrashid/implementing-keycloak-event-listener-spi-service-provider-interfaces-1f01ae819e8d
* My Guide in how to use REST with Keycloak: https://medium.com/@jawadrashid/how-to-use-rest-api-for-keycloak-admin-through-node-js-app-cfac0372eb4a
* Setting up AWS cli: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
* AWS Lambda Documentation for Deploying Lambda Function with container images: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html
* Great Keycloak with docker YouTube playlist by code215: https://www.youtube.com/playlist?list=PLQZfys2xO5kgpa9-qpJly78d-t7_Fnjec
* AWS Lambda Tutorial For Beginners by Simplilearn: https://www.youtube.com/watch?v=97q30JjEq9Y
* Keycloak Admin Client Node.JS Client Library: https://www.npmjs.com/package/@keycloak/keycloak-admin-client
* Keycloak admin Client Node.JS github documentation: https://github.com/keycloak/keycloak/tree/main/js/libs/keycloak-admin-client
* AWS Lambda Documentation for Deploying Lambda Function with container images in Java: https://docs.aws.amazon.com/lambda/latest/dg/java-image.html
* Keycloak Java Classes Documentation: https://www.keycloak.org/docs-api/25.0.2/javadocs/
* Keycloak Java Baeldung tutorial on Search Users with keycloak in Java Spring: https://www.baeldung.com/java-keycloak-search-users