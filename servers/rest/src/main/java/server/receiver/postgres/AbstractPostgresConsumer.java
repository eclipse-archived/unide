/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.receiver.postgres;

import java.util.function.Consumer;

/**
 * Base class for postgres {@link Consumer}
 *
 * @param <T> - Type of Object to consume
 */
abstract class AbstractPostgresConsumer<T> implements Consumer<T> {

   private final JdbcExecutorService executorService;

   /**
    * @param executorService - the executorService
    */
   AbstractPostgresConsumer( JdbcExecutorService executorService ) {
      this.executorService = executorService;
   }

   JdbcExecutorService getExecutorService() {
      return executorService;
   }

   /**
    * @param object - object to check
    * @return true when given object is not null
    */
   static boolean isNotNull( Object object ) {
      return null != object;
   }

}
