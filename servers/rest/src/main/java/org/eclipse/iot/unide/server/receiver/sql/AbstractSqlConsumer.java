/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package org.eclipse.iot.unide.server.receiver.sql;

import java.util.function.Consumer;

/**
 * Base class for sql {@link Consumer}
 *
 * @param <T> - Type of Object to consume
 */
abstract class AbstractSqlConsumer<T> implements Consumer<T> {

   private final JdbcExecutorService executorService;

   /**
    * @param executorService - the executorService
    */
   AbstractSqlConsumer( JdbcExecutorService executorService ) {
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
