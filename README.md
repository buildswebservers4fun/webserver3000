# _Builds Webservers 4 Fun_
[![build status](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/badges/master/build.svg)](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/commits/master)
[![coverage report](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/badges/master/coverage.svg)](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/commits/master)

This project is a Software Architecture project (csse477) for Chandan Rupakheti at Rose-Hulman Institute of Technology. In this project, we are building a web server that handles the standard requests (GET, POST, PUT, HEAD) from given barebones code. This web server now allows plugins to be attached to use custom behavior. Our web server also now implements 8 tactics to improve availability, security, and performance. The authors of this project are Trent Punt, CJ Miller, and Davis Nygren.


## Team Members
Sean McPherson
CJ Miller
Davis Nygren
Trent Punt

## Architecture
![Module Diagram](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/raw/master/Docs/moduleDiagram.JPG "Module Diagram")
Our system utilizes an app server and plugin loader to allow for the loading of new plugins at runtime. We also utilize our own protocol for the plugins to use to ensure that requests and responses are understood between them. 

## Detailed Design
![UML of Webserver](https://ada.csse.rose-hulman.edu/buildswebservers4fun/webserver3000/raw/master/Docs/diagram.png "UML of Webserver")

A single plugin can register as many servlets as it wants. There is also sub Interfaces for each of IServlet’s methods which can help with understanding of flow in a plugin or allow for easy reuse. The server is also detached from having to understand anything about how plugins are loaded. It just requests the IServlet for a given path and the Router takes care of the prioritization of the routes.

## Scenarios
#### Server Restarts (Availability)
Server is turned off and back on due to an action of an admin or automated tool

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Admin|Request Restart|Normal Use|Server|Server Shuts down then comes back up|How long can users requests not be fulfilled|

#### Correct Responses (Availability)
Client requests a file and they want that file back in its actual form

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Normal Users|Users request file from server|Stressed Use|Server|Server responds to all clients|Percent of responses that are not correct|

#### Server Crashes (Security)
Server goes down due to malicious users or some bug in the server or plugin

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Bad User|User sends requests that crashes the server|Normal Conditions|Server|Server Restarts|Time between crash and server able to accept requests again|

#### Prioritized Requests (Security)
Server is being bombarded by clients, the clients that are legitimate and not bombarding us should still   be able to get what they want

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Normal Users|Users send requests|Stressed Conditions with malicious Users|Server|Server responds to clients|Responses per Second|

#### Latency (Performance)
User wants their requests completed in a timely manner

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Normal User|User sends request|Normal Conditions|Server|Server send the response for the request|Time to Return|
#### Service Rate (Performance)
The throughput of the server/how many requests can it process per second?

| Source  	| Stimulus | Environment | Artifacts | Response | Response Measure |
|-------------|----------|-------------|-----------|----------|------------------|
|Multiple Normal Users|Users send requests|Stressed Rate Conditions|Server|Server responds to all users requests|Requests responded to per second|

### Improvement Tactics

* Auto-Restart
  * Cron Job queries the service to see if the program is running (Does not do any http request). If it is not running it starts it.  
  * Server Crashes Scenario.
* Cache Layer
  * Records all successful GET requests and stores them in memory for a configurable amount of time. All subsequent requests within the time limit will grab the file from memory.
  * Latency and Service rate Scenarios
* HeartBeat
 * Makes a request to our server every certain amount of time and checks for a response. If the server does not respond for a certain amount of times in a row, it will kill our server and restart it automatically. The user can set up how often the process sends heartbeats and how many failures in a row makes the server die.
*server crashes scenario
* HTTPS
* The server now has https implemented on port 443 with a self-signed certificate
*correct response scenario
* Priority Queue
 * The server prioritized quick small requests over larger, slower requests.
* DOS Defense
 * Server maintains an IP blacklist and refuses to send responses to blacklisted IPs.
* Rabbit MQ
  *Forwards the requests/responses to a RabbitMQ queue as a byte array where it may be further distributed to workers (or servlets) to be handled. Useful for improving performance.
* Time-Stamping
  * Adds additional content to the headers of both requests and responses to include a “Time Sent” or “Time Received” respectively, which may be retrieved later by the connection handler or simply viewed by a client (using Postman, for example). Helps to improve availability by providing feedback for the client.

| | Server Restart (Seconds) | Correct Responses | Server Crashes   	| Prioritized users | "Latency (Sets of 1000 GET PUT or POST requests)" | "Service rate (1000 users at 200 hatch rate for 10 000 requests)" |
|----------|--------|---------|---------------|------------|------|-------|
| Baseline         	| 0.88903708           	| 97.53%        	| Untill Admin Reboots |               	|                          25.05805 | 885   |                 	|
| Auto-Restart     	| 0.88903708           	| 97.53%        	| 30s              	|               	|                           24.9846 | 885   |                 	|
| Cache Layer      	| 0.882026116          	| 98.14%        	| Untill Admin Reboots |               	|                            14.655 | 787.6 |                 	|
| HeartBeat        	| 0.892043772          	| 99.87%        	| Untill Admin Reboots |               	|                           20.2194 | 827.5 |                 	|
| Https            	| 0.960364606          	| 98.36%        	| Untill Admin Reboots |               	|                         19.839839 | 682.4 |                 	|
| Priority Queue   	| 0.868692338          	| 99.01%        	| Untill Admin Reboots |               	|                            32.542 | 694.8 |                 	|
| DDOS Defense     	| 0.912117889          	| 99.47%        	| Untill Admin Reboots |               	|                         21.854709 | 790.2 |                 	|
| Rabbit MQ        	| 0.895069078          	| 98.71%        	| Untill Admin Reboots |               	|                         20.219438 | 801.5 |                 	|
| Time-Stamping    	| 0.897740023          	| 98.89%        	| Untill Admin Reboots |               	|                          23.84468 | 859.1 |                 	|
| All Tactics combined |  |  | 30s  |  |  |  |  |

### Result

* Server Restart
  * Calculated by Recording the time then telling the server to stop then start. It then repeatedly requests a file from the server until it responds. It then calculates the time difference.
* Correct Responses
  * Calculated by using Jmeter to use 7000 hosts to make get requests to our production server for 2 minutes and check the reported error percentage from Jmeter’s statistics
* Server Crashes
  * Crashes the server by having a plugin call System.Exit(). Then measure the time until the server can accept requests again
* Prioritized users
* Latency
  * Calculated by subtracting the time a response was received from the time the corresponding request was sent (in milliseconds). Able to be run in-house using simple test suite functions. 
* Service Rate
  * Calculated by using Jmeter to use 7000 hosts to make get requests to our production server for 2 minutes and check the reported throughput from Jmeter’s statistics
* GRAB FROM DRIVE *





### Future Improvements

* Have a queue thread instead of each connection getting its own thread
* Pre-fetching
* Make our system a distributed system and add more servers to our system
* Have redundancy available in the form of hot or cold spares of our webserver
* Implement load balancing
* Buy a server monitoring tool to track statistics such as CPU use, memory, storage, etc to better understand our web server’s traffic and help resolve bottlenecks at peak times
* Get an actually certified ssl certificate so users trust our webserver


API

Feature	1:	Retrieving	a	list	of	users
Method:	 GET
URI: /UserID/
Request	Body:
<none>
Response	Body:
{
  “code”:	200, [NOTE:	This	could	be	app	specific	also]
  “message”:	“Ok”,
  “payload”:	
  [
    { “id”:	7,	“name”:	“James	Bond”, "email": "notjamesbond@gmail.com"},
    {	“id”:	8,	“name”:	“Jason	Bourne”, "email": "jason@bourne.com"}
  ]
}

Feature 2: Retrieving	a	user
Method:	 GET
URI: /UserID/7
Request Body:
<none>
Response	Body:
{
  “code”:	200,
  “message”:	“Ok”,
  “payload”:	{	“id”:	7,	“name”:	“James	Bond”, "email":"notjamesbond@gmail.com"}
}
OR
{
  “code”:	404,
  “message”:	“Not	Found”
}

Feature 3: Creating	a	new	user
Method:	 POST
URI: /UserId/
Request Body:
{	“name”:	“Spongebob Squarepants”, "email", "Ilovegary@gmail.com"}
Response Body:
{
  “code”:	201,
  “message”:	“Created”,
  “payload”:	{	“id”:	9,	“name”:	“Spongebob Squarepants”, "Ilovegary@gmail.com"}
}

Feature 4: Editing a user
Method: PUT
URI: /UserID/9
RequestBody:
{"name”:	“Spongebob Squarepants”, "email", "patrickstar@gmail.com"}
Response Body:
{
  “code”:	200,
  “message”:	“Ok",
  “payload”:	{	“id”:	9,	“name”:	“Spongebob Squarepants”, "patrickstar@gmail.com"}
}

Feature 5: Deleting a user
Method: DELETE
URI: /UserID/9
RequestBody:
<none>
Response Body:
{
  "code": 200,
  "message", "Ok"
}
OR

{
  “code”:	404,
  “message”:	“Not	Found”
}