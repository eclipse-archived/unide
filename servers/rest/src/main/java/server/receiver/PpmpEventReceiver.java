/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver;

import java.util.function.Consumer;

import org.eclipse.iot.unide.ppmp.PPMPPackager;
import org.eclipse.iot.unide.ppmp.measurements.MeasurementsWrapper;
import org.eclipse.iot.unide.ppmp.messages.MessagesWrapper;
import org.eclipse.iot.unide.ppmp.process.ProcessWrapper;

import server.ppmp.PpmpEvent;
import server.ppmp.PpmpType;

/**
 *  Base class for {@link Receiver} implementations.
 *  {@link PpmpEvent} payloads are converted and delegated to the corresponding consumers.
 *  The consumer implementations have to be provided by the implementation classes.
 */
public  abstract class PpmpEventReceiver implements  Receiver{

   private PPMPPackager ppmpMapper;

   public PpmpEventReceiver(PPMPPackager ppmpMapper){
      this.ppmpMapper = ppmpMapper;
   }

   @Override
   public void handle( PpmpEvent ppmpEvent ) throws ReceiverException {
      try {
         PpmpType ppmpType = ppmpEvent.getPpmpType();
         String ppmpPayload = ppmpEvent.getPayload();
         switch ( ppmpEvent.getPpmpType() ) {
            case MESSAGE:
               MessagesWrapper messagesWrapper = ppmpMapper.getMessagesBean( ppmpPayload );
               getMessagesConsumer().accept( messagesWrapper );
               break;
            case MEASUREMENT:
               MeasurementsWrapper measurementsWrapper = ppmpMapper.getMeasurementsBean( ppmpPayload );
               getMeasurementsConsumer().accept( measurementsWrapper );
               break;
            case PROCESS:
               ProcessWrapper processWrapper = ppmpMapper.getProcessesBean( ppmpPayload );
               getProcessesConsumer().accept( processWrapper );
               break;
            default:
               throw new IllegalArgumentException( "Unknown type '" + ppmpType + "'" );
         }
      } catch ( Exception e ) {
         throw new ReceiverException( "Failed to handle message", e );
      }
   }

   protected abstract Consumer<MessagesWrapper> getMessagesConsumer();
   protected abstract Consumer<MeasurementsWrapper> getMeasurementsConsumer();
   protected abstract Consumer<ProcessWrapper> getProcessesConsumer();

}
