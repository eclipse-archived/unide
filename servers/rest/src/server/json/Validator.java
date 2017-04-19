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

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

/**
 * Validates a JSON-String to a given PPMP-JSON schema
 */
public class Validator {
    
	private static final Validator validator = new Validator(); 
    
	/**
	 * Map for content-spec and the JSON-schema to use
	 */
    final static Map<String, String> contentSpecMapping = new HashMap<String, String>();
    
    static{
    	contentSpecMapping.put("urn:spec://eclipse.org/unide/machine-message#v2", "message_schema_v2.json");
    	contentSpecMapping.put("urn:spec://eclipse.org/unide/measurement-message#v2", "measurement_schema_v2.json");
    	contentSpecMapping.put("urn:spec://bosch.com/ppm/process-message#v2", "process_message_v2.json");
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
     */
    private Schema getSchema(JSONObject jsonObject) throws IOException{
    	
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
    private InputStream getSchemaStream(JSONObject jsonObject) throws IOException{
    	
    	InputStream schemaStream = null;
    	
    	schemaStream = Validator.class.getClassLoader().getResourceAsStream(contentSpecMapping.get(jsonObject.getString("content-spec")));

        return schemaStream;
    }
 
}