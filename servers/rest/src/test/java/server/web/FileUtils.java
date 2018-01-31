/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.web;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

class FileUtils {
   static String readFile( String filename ) {
      ClassLoader classLoader = FileUtils.class.getClassLoader();
      try {
         return IOUtils.toString( classLoader.getResourceAsStream( filename ), Charset.defaultCharset() );
      } catch ( IOException e ) {
         throw new RuntimeException( e );
      }
   }
}
