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

import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;


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
    private Schema getSchema(String schemaText) throws IOException
    {
        final JSONObject schemaNode = new JSONObject(schemaText);
        return SchemaLoader.load(schemaNode);
    }
    
    /**
     * Validates the PPMP JSON string to the specific schema 
     * @param jsonText PPMP message as JSON string
     * @throws ValidationException
     * @throws IOException
     */
    public void validate(String jsonText) throws ValidationException, IOException
    {    	    	
        final JSONObject jsonNode = new JSONObject(jsonText.replace("content_spec", "content-spec"));
    	
        final Schema jsonSchema = getSchema(jsonNode); 
        
        jsonSchema.validate(jsonNode);  
    }
    
    /**
     * Returns the correct schema for the given JSON 
     * @param jsonObject JSON object
     * @return Schema which maps to the object
     * @throws IOException
     * @throws ValidationException 
     */
    private Schema getSchema(JSONObject jsonObject) throws IOException, ValidationException{
    	
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
    private InputStream getSchemaStream(JSONObject jsonObject) throws IOException {
    	
    	InputStream schemaStream = null;
    	String contentSpec = jsonObject.getString("content-spec");
    	
    	assert !contentSpec.isEmpty();

    	schemaStream = Validator.class.getResourceAsStream("/" + contentSpecMapping.get(contentSpec));
    	
    	if (schemaStream == null){
    		LOG.info("No validation schema found for content-spec: " + contentSpec);
    		throw new IOException("No validation schema found for content-spec: " + contentSpec + ".");
    	}

        return schemaStream;
    }
    
}