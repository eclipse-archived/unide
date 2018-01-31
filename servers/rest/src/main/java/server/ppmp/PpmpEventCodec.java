/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.ppmp;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Codec for {@code PpmpEvent}.
 *
 * This codec have to be registered on the vert.x eventBus to enable {@link PpmpEvent} as message types.
 */
public class PpmpEventCodec implements MessageCodec<PpmpEvent, PpmpEvent> {

   private static final String PPMP_TYPE = "ppmpType";
   private static final String PPMP_PAYLOAD = "ppmpPayload";

   @Override
   public void encodeToWire( Buffer buffer, PpmpEvent ppmpEvent ) {
      // Easiest ways is using JSON object
      JsonObject jsonToEncode = new JsonObject();
      jsonToEncode.put( PPMP_TYPE, ppmpEvent.getPpmpType().name() );
      jsonToEncode.put( PPMP_PAYLOAD, ppmpEvent.getPayload() );

      // Encode object to string
      String jsonToStr = jsonToEncode.encode();

      // Length of JSON: is NOT characters count
      int length = jsonToStr.getBytes().length;

      // Write data into given buffer
      buffer.appendInt( length );
      buffer.appendString( jsonToStr );
   }

   @Override
   public PpmpEvent decodeFromWire( int position, Buffer buffer ) {
      int pos = position;
      int length = buffer.getInt( pos );
      String jsonStr = buffer.getString( pos += 4, pos += length );
      JsonObject contentJson = new JsonObject( jsonStr );
      PpmpType ppmpType = PpmpType.valueOf( contentJson.getString( PPMP_TYPE ) );
      String ppmpPayload = contentJson.getString( PPMP_PAYLOAD );
      return new PpmpEvent( ppmpType, ppmpPayload );
   }

   @Override
   public PpmpEvent transform( PpmpEvent ppmpEvent ) {
      return ppmpEvent;
   }

   @Override
   public String name() {
      return this.getClass().getSimpleName();
   }

   @Override
   public byte systemCodecID() {
      return -1;
   }
}
