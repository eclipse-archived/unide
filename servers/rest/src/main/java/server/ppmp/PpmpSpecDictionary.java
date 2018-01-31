/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.ppmp;

import java.util.Collection;

import javax.xml.bind.ValidationException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Ppmp Protocol types have to contain the type information of the message. The field content-spec contains this
 * information.
 *
 * This class contains mappings to map the content-spec attribute in a ppmp - mesasge to internal {@link PpmpType}.
 */
class PpmpSpecDictionary {

   /**
    * Schemas are located in the ppmp-schema dependency.
    */
   private static final String ROOT_SCHEMA_LOCATION = "org/eclipse/iot/unide/ppmp/v2/";
   private static final String MESSAGES_SCHEMA_LOCATION = ROOT_SCHEMA_LOCATION + "message_schema.json";
   private static final String MEASUREMENTS_SCHEMA_LOCATION = ROOT_SCHEMA_LOCATION + "measurement_schema.json";
   private static final String PROCESSES_SCHEMA_LOCATION = ROOT_SCHEMA_LOCATION + "process_schema.json";

   private PpmpSpecDictionary() {
   }

   /**
    * Map for content-spec and the JSON-schema to use
    */
   private static Multimap<String, PpmpType> CONTENT_SPEC_MAPPING;

   static {
      Multimap<String, PpmpType> tmp = ArrayListMultimap.create();
      tmp.put( "urn:spec://eclipse.org/unide/machine-message#v2", PpmpType.MESSAGE );
      tmp.put( "urn:spec://bosch.com/ppm/machine-message#v2", PpmpType.MESSAGE );
      tmp.put( "urn:spec://eclipse.org/unide/measurement-message#v2", PpmpType.MEASUREMENT );
      tmp.put( "urn:spec://bosch.com/ppm/measurement-message#v2", PpmpType.MEASUREMENT );
      tmp.put( "urn:spec://eclipse.org/unide/process-message#v2", PpmpType.PROCESS );
      tmp.put( "urn:spec://bosch.com/ppm/process-message#v2", PpmpType.PROCESS );
      CONTENT_SPEC_MAPPING = tmp;
   }

   /**
    * Returns the resource name for the given type
    *
    * @param ppmpType - to get the resource for
    * @return the resource name
    */
   static String getResourceName( PpmpType ppmpType ) {
      switch ( ppmpType ) {
         case MESSAGE:
            return MESSAGES_SCHEMA_LOCATION;
         case MEASUREMENT:
            return MEASUREMENTS_SCHEMA_LOCATION;
         case PROCESS:
            return PROCESSES_SCHEMA_LOCATION;
         default:
            throw new IllegalArgumentException( "Unknown type '" + ppmpType + "'" );
      }
   }

   /**
    * @param contentSpec - the content-spec value
    * @return the corresponding type for given contentSpec
    */
   static PpmpType getPpmpType( String contentSpec ) throws ValidationException {
      Collection<PpmpType> ppmpTypes = CONTENT_SPEC_MAPPING.get( contentSpec );
      if ( ppmpTypes.isEmpty() ) {
         throw new ValidationException( "For contentSpec '" + contentSpec + "' no type mapping exists" );
      }
      return ppmpTypes.iterator().next();
   }

}
