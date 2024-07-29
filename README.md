# Using AWS Lambda with Keycloak

Go to the docker folder and run command 
```
docker-compose down --remove-orphans
```
Then to run the docker run command:
```
docker-compose up --build -d
```
The base of this tutorial are videos upto setting up nginx i.e. upto video 8 on this playlist by code215 [https://www.youtube.com/playlist?list=PLQZfys2xO5kgpa9-qpJly78d-t7_Fnjec](https://www.youtube.com/playlist?list=PLQZfys2xO5kgpa9-qpJly78d-t7_Fnjec)

Instructions on how to create Lambda in NodeJS
[https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html](https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html)

# Lambda Code
Run the following code to run lambda code which is given in this guide: [https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html](https://docs.aws.amazon.com/lambda/latest/dg/nodejs-image.html). We need to send the username to search for in Body. You can also set "realm" key and value to change the default realm like {"username": "user", "realm": "myrealm"}
```
Invoke-WebRequest -Uri "http://localhost:9000/2015-03-31/functions/function/invocations" -Method Post -Body '{"username": "user"}' -ContentType "application/json"
```

You can also test using Postman by setting method as POST with url: [http://localhost:9000/2015-03-31/functions/function/invocations](http://localhost:9000/2015-03-31/functions/function/invocations) with Body set as 'raw' and JSON with contents:

```
{
    "username": "user",
    "realm": "myrealm"
}
```
Realm is optional

# Permissions
Assign manage_users and view_users service account role to the demo-client or whatever your client for this app is. Client with client authorization and authentication needs to be enabled for this code to work on master realm.

# NodeJS Lambda 

## Node_modules
Remove node_modules folder and run npm install to install keycloak admin library. 

## Documentation for Keycloak NodeJS Admin Client
[https://www.npmjs.com/package/@keycloak/keycloak-admin-client](https://www.npmjs.com/package/@keycloak/keycloak-admin-client)

## Terminate Node Service and rerun
```
docker-compose stop lambda-node
docker-compose up lambda-node --build -d
```

## How to run getSessions and terminateSessions lambda functions in Node JS
In docker/lambda-node you need to make 2 changes:

1. In package.json change the value of "main" to value of "getSessions.mjs" or "terminateSessions.mjs" to run the correct code. These values are given in keys "lambda1" and "lambda2"
2. In dockerfile inside lambda-node change the last line to one of the following based on what code you want to run:

For getSessions
```
CMD [ "getSessions.handler" ]
```
For terminateSessions
```
CMD [ "terminateSessions.handler" ]
```

## How to run the scripts in NodeJS

You will need to upload a zip with your mjs file and node_modules. Run command **npm install** in lambda-node directory to install node_modules. 
You will need to modify mjs file starts to change the const defined in first few lines to match your keycloak setup. 
This code has been tested with keycloak without SSL. 

You can set environment variables in your lambda function configuration or change the following variables:

* BASE_URL
* KEYCLOAK_CLIENT_ID
* KEYCLOAK_CLIENT_SECRET
* KEYCLOAK_REALM

# Java Code Details

All the code for lambda java is in docker/lambda-java folder. I followed this guide [https://docs.aws.amazon.com/lambda/latest/dg/java-image.html](https://docs.aws.amazon.com/lambda/latest/dg/java-image.html) to setup lambda in Java. 

## Commands to create Java lambda function

```
mvn -B archetype:generate `
   "-DarchetypeGroupId=software.amazon.awssdk" `
   "-DarchetypeArtifactId=archetype-lambda" "-Dservice=s3" "-Dregion=US_WEST_2" `
   "-DgroupId=com.keycloak.sessionsapp" `
   "-DartifactId=getsessions"
```

```
mvn compile dependency:copy-dependencies -DincludeScope=runtime
```

```
docker build --platform linux/amd64 -t docker-image:test .
```

Use docker-compose up in docker folder to run the server.

Use Postman to POST request at url: [http://localhost:9100/2015-03-31/functions/function/invocations](http://localhost:9100/2015-03-31/functions/function/invocations) for getsessions and [http://localhost:9200/2015-03-31/functions/function/invocations](http://localhost:9100/2015-03-31/functions/function/invocations) for terminatesessions. 

Use JSON body in Postman request like in NodeJS
```
{
    "username": "user"
}
```

You can invoke the requests by using cmd also like this for powershell for getsession and termiantesessions respectively:
For getting Sessions
```
Invoke-WebRequest -Uri "http://localhost:9100/2015-03-31/functions/function/invocations" -Method Post -Body '{"username": "user"}' -ContentType "application/json"
```
For terminating Sessions
```
Invoke-WebRequest -Uri "http://localhost:9200/2015-03-31/functions/function/invocations" -Method Post -Body '{"username": "user"}' -ContentType "application/json"
```

## Guide to setup call Keycloak API through Java

[https://www.baeldung.com/java-keycloak-search-users](https://www.baeldung.com/java-keycloak-search-users)