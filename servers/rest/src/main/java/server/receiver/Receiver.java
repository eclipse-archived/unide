/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver;

import server.ppmp.PpmpEvent;

/**
 * Handles {@link PpmpEvent}
 */
public interface Receiver {
   /**
    * Called on application startup to do init tasks.
    *
    * @throws ReceiverException - When something went wrong during the init
    */
   void init() throws ReceiverException;

   /**
    * Handles the given event
    *
    * @param event - The event to handle
    * @throws ReceiverException - When something went wrong during the processing
    */
   void handle( PpmpEvent event ) throws ReceiverException;
}
