/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.ppmp;

/**
 * Contains the payload of a PPMP message and the type of the message.
 *
 * An object of this class can be send over the vert.x eventbus when the codec with {@link PpmpEventCodec} is registered.
 */
public class PpmpEvent {
   private final PpmpType ppmpType;
   private final String payload;

   public PpmpEvent( PpmpType ppmpType, String payload ) {
      this.ppmpType = ppmpType;
      this.payload = payload;
   }

   public PpmpType getPpmpType() {
      return ppmpType;
   }

   public String getPayload() {
      return payload;
   }

   @Override
   public String toString() {
      return "PpmpEvent{" +
            "ppmpType=" + ppmpType +
            ", payload='" + payload + '\'' +
            '}';
   }
}
