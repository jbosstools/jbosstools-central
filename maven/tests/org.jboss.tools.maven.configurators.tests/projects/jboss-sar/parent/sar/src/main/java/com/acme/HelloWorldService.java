package com.acme;

import org.apache.commons.lang.StringUtils;



public class HelloWorldService implements HelloWorldServiceMBean
{
   // Our message attribute
   private String message = "Sorry no message today";

   // Getters and Setters
   public String getMessage()
   {
      return message;
   }
   
   public void setMessage(String message)
   {
      this.message = message;
   }

   // The printMessage operation
   public void printMessage()
   {
      System.out.println(message);
   }

   // The lifecycle
   public void start() throws Exception
   {
      System.out.println("Démarrage avec le message=" + StringUtils.reverse(message));
   }
   
   public void stop()
   {
      System.out.println("Stopping with message=" + StringUtils.reverse(message));
   }
}
