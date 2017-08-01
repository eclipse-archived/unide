/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package server.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;


/**
 * Validates a JSON-String to a given PPMP-JSON schema
 */
public class Validator {
    
	private static final Validator validator = new Validator(); 
	private static final Logger LOG = Logger.getLogger( Validator.class );
    
	/**
	 * Map for content-spec and the JSON-schema to use
	 */
    final static Map<String, String> contentSpecMapping = new HashMap<String, String>();
    
    static{
    	contentSpecMapping.put("urn:spec://eclipse.org/unide/machine-message#v2", "message_schema_v2.json");
    	contentSpecMapping.put("urn:spec://eclipse.org/unide/measurement-message#v2", "measurement_schema_v2.json");
    	contentSpecMapping.put("urn:spec://eclipse.org/unide/process-message#v2", "process_message_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/ppm/process-message#v2", "process_message_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/ppm/measurement-message#v2", "measurement_schema_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/ppm/machine-message#v2", "message_schema_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/cindy/process-message#v2", "process_message_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/cindy/measurement-message#v2", "measurement_schema_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/cindy/machine-message#v2", "message_schema_v2.json");
    }
	
    private Validator() {
    	
    }
         
    public static Validator getInstance() { 
      return validator; 
    } 
        
    /**
     * Converts a text to a schema object
     * @param schemaText Schema as string in JSON format
     * @return schema object
     * @throws IOException
     */
    private JsonSchema getSchema(String schemaText) throws IOException
    {
    	JsonSchemaFactory jsonSchemafactory = new JsonSchemaFactory();
    	
        JsonSchema jsonSchema = jsonSchemafactory.getSchema(schemaText);
        
        return jsonSchema;
    }
    
    /**
     * Validates the PPMP JSON string to the specific schema 
     * @param jsonText PPMP message as JSON string
     * @throws ValidationException
     * @throws IOException
     */
    public void validate(String jsonText) throws IllegalArgumentException, ValidationException, IOException
    {    	    	
        final JsonNode jsonNode = new ObjectMapper().readTree(jsonText.replace("content_spec", "content-spec"));
    	
        final JsonSchema jsonSchema = getSchema(jsonNode); 
        
        Set<ValidationMessage> messages = jsonSchema.validate(jsonNode);  
        
        if (messages.size() > 0) {
        	
        	ObjectNode jNode = new ObjectMapper().createObjectNode();

        	StringBuilder message = new StringBuilder();       
        	
        	for (ValidationMessage validationMessage : messages) {
        		jNode.put(validationMessage.getMessage().replace("$.", "").split(": ")[0], validationMessage.getMessage().replace("$.", "").split(": ")[1]);
			}
        	
        	message.append(jNode);
        	
        	throw new ValidationException(message.toString());
        }
        
    }
    
    /**
     * Returns the correct schema for the given JSON 
     * @param jsonObject JSON object
     * @return Schema which maps to the object
     * @throws IOException
     * @throws ValidationException 
     */
    private JsonSchema getSchema(JsonNode jsonObject) throws IOException, ValidationException{
    	
    	InputStream schemaStream = getSchemaStream(jsonObject);
    	StringBuilder schema = new StringBuilder();
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream));
        
        String line;
        
        while ((line = reader.readLine()) != null) {
        	schema.append(line);
        }    	
        
        schemaStream.close();
        
        return getSchema(schema.toString());
    }
    
    /**
     * Returns the stream of the correct schema file
     * @param jsonObject JSON object to find the correct schema
     * @return JSON schema as stream 
     * @throws IOException
     */
    private InputStream getSchemaStream(JsonNode jsonNode) throws IOException {
    	
    	InputStream schemaStream = null;
    	JsonNode contentSpec = jsonNode.get("content-spec");
    	
    	assert !contentSpec.textValue().isEmpty();

    	if (contentSpec != null){
    		schemaStream = Validator.class.getResourceAsStream("/" + contentSpecMapping.get(contentSpec.textValue()));
    	} 
    	else{
    		LOG.info("No content-spec found");
    		throw new IOException("No content-spec found.\n"
    				+ "Valid content-specs are:\n"
    				+ "urn:spec://eclipse.org/unide/machine-message#v2\n"
    				+ "urn:spec://eclipse.org/unide/measurement-message#v2\n"
    				+ "urn:spec://eclipse.org/unide/process-message#v2");
    	}
    	
    	
    	if (schemaStream == null){
    		LOG.info("No validation schema found for content-spec: " + contentSpec);
    		throw new IOException("No validation schema found for content-spec: " + contentSpec + ".\n"
    				+ "Valid content-specs are:\n"
    				+ "urn:spec://eclipse.org/unide/machine-message#v2\n"
    				+ "urn:spec://eclipse.org/unide/measurement-message#v2\n"
    				+ "urn:spec://eclipse.org/unide/process-message#v2");
    	}

        return schemaStream;
    }
    
}