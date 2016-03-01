# Shopping Cart Microservice

This project provides an example of a statefull REST microservice build using Spray, Akka, Scala and per-request pattern 
(based on the corresponding typesafe activator template).  

## Example App

### Overview

This example application provides an API for managing shopping carts. There are 2 services: one manages shopping carts, 
another one - managing shopping cart items. An new instance of a shopping cart items managing service is created every time
a new shopping cart is created. 

### Running

    sbt run

### Successful requests

To create a new shopping cart:

	POST http://localhost:8080/carts

We get a successful 201 Created response. The id of a newly created shopping cart is returned in response body:
	
	{
	  "cart":{
	    "id":"0dd8e088-45ec-4ca8-ad9f-ad6fa081027d"
	  }
	}

To list shopping carts:

	GET http://localhost:8080/carts

To see a shopping cart content:
	
	GET http://localhost:8080/carts/0dd8e088-45ec-4ca8-ad9f-ad6fa081027d

To add an item to an existing shopping cart:	
	
	PUT http://localhost:8080/carts/0dd8e088-45ec-4ca8-ad9f-ad6fa081027d/items/lamp
	
To remove an item from an existing shopping cart:

	DELETE http://localhost:8080/carts/0dd8e088-45ec-4ca8-ad9f-ad6fa081027d/items/lamp

To delete a shopping cart:
	
	DELETE http://localhost:8080/carts/0dd8e088-45ec-4ca8-ad9f-ad6fa081027d
