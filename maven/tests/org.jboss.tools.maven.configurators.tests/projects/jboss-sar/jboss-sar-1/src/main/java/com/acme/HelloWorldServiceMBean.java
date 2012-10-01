package com.acme;


public interface HelloWorldServiceMBean
{
   // Configure getters and setters for the message attribute
   String getMessage();
   void setMessage(String message);
   
   // The print message operation
   void printMessage();
   
   // Lifecycle callbacks
   void start() throws Exception;
   void stop();
}
