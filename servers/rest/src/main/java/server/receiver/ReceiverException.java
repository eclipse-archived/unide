/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver;

/**
 * Custom exception for {@link Receiver}.
 */
public class ReceiverException extends Exception {
   public ReceiverException( String message, Throwable cause ) {
      super( message, cause );
   }
}
