/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.ppmp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import javax.xml.bind.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

/**
 * Validates a JSON-String to a given PPMP-JSON schema
 */
public class PpmpValidator {

   private PpmpValidator() {

   }

   /**
    * Validates the PPMP JSON string to the specific schema
    *
    * @param ppmpJson PPMP message as JSON string
    * @throws ValidationException - when given json is not valid ppmp message
    */
   public static PpmpEvent validate( String ppmpJson ) throws ValidationException {
      JsonNode jsonNode = toJsonNode( ppmpJson );
      PpmpType ppmpType = getPpmpType( ppmpJson );
      Set<ValidationMessage> messages = validate( ppmpType, jsonNode );
      if ( messages.size() == 0 ) {
         return new PpmpEvent( ppmpType, ppmpJson );
      }
      throw new ValidationException( buildValidationFailedMessage( messages ) );
   }

   public static PpmpType getPpmpType( String ppmpJson ) throws ValidationException {
      return getPpmpType( toJsonNode( ppmpJson ) );
   }

   private static JsonNode toJsonNode( String ppmpJson ) {
      String jsonToConvert = ppmpJson.replace( "content_spec", "content-spec" );
      try {
         return new ObjectMapper().readTree( jsonToConvert );
      } catch ( IOException e ) {
         throw new RuntimeException( e );
      }
   }

   private static PpmpType getPpmpType( JsonNode ppmpJson ) throws ValidationException {
      JsonNode contentSpec = ppmpJson.get( "content-spec" );
      if ( null == contentSpec ) {
         throw new ValidationException( "Attribute content-spec is required" );
      }
      return PpmpSpecDictionary.getPpmpType( contentSpec.textValue() );
   }

   private static String buildValidationFailedMessage( Set<ValidationMessage> messages ) {
      ObjectNode jNode = new ObjectMapper().createObjectNode();
      StringBuilder message = new StringBuilder();
      for ( ValidationMessage validationMessage : messages ) {
         jNode.put( validationMessage.getMessage().replace( "$.", "" ).split( ": " )[0],
               validationMessage.getMessage().replace( "$.", "" ).split( ": " )[1] );
      }
      message.append( jNode );
      return message.toString();
   }

   private static Set<ValidationMessage> validate( PpmpType ppmpType, JsonNode jsonToValidate ) {
      try {
         JsonSchemaFactory jsonSchemafactory = new JsonSchemaFactory();
         JsonSchema jsonSchema = jsonSchemafactory.getSchema( getSchemaResource( ppmpType ) );
         return jsonSchema.validate( jsonToValidate );
      } catch ( IOException ex ) {
         throw new RuntimeException( ex );
      }
   }

   private static String getSchemaResource( PpmpType ppmpType ) throws IOException {
      String resourceName = PpmpSpecDictionary.getResourceName( ppmpType );
      InputStream resource = PpmpValidator.class.getClassLoader()
                                                .getResourceAsStream( PpmpSpecDictionary.getResourceName( ppmpType ) );
      if ( resource == null ) {
         throw new FileNotFoundException( "File " + resourceName + " not found." );
      }
      return readFromInputStream( resource );
   }

   private static String readFromInputStream( InputStream inputStream )
         throws IOException {
      StringBuilder resultStringBuilder = new StringBuilder();
      try ( BufferedReader br
            = new BufferedReader( new InputStreamReader( inputStream ) ) ) {
         String line;
         while ( (line = br.readLine()) != null ) {
            resultStringBuilder.append( line ).append( "\n" );
         }
      }
      return resultStringBuilder.toString();
   }
}