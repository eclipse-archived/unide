/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package server;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.apache.commons.io.IOUtils;

/**
 * Unit Tests for REST-endpoints
 */
@RunWith(VertxUnitRunner.class)
public class RestEndpointTests extends AbstractVerticle{

	private static final String TEST_FILE_NAME = "test_message.json";
	private static String TEST_MESSAGE;
	private static final int port = 8080;

	@BeforeClass
	public static void readFileContent() throws IOException {
		ClassLoader classLoader = RestEndpointTests.class.getClassLoader();
		TEST_MESSAGE = IOUtils.toString(classLoader.getResourceAsStream(TEST_FILE_NAME),Charset.defaultCharset());
	}
	
	@Rule
	public final RunTestOnContext rule = new RunTestOnContext(Vertx::vertx);
	
	/**
	 * Setup verticle deployment
	 * @param context
	 * @throws IOException
	 */
	@Before
	public void setUp(TestContext context) throws IOException {
	  vertx = Vertx.vertx();
	  DeploymentOptions options = new DeploymentOptions()
	      .setConfig(new JsonObject().put("http.port", port)
	      );
	  vertx.deployVerticle(server.endpoints.RestEndpoints.class.getName(), options, context.asyncAssertSuccess());
	}
	
	/**
	 * Closing verticle at the end
	 * @param context
	 */
	@After
	public void tearDown(TestContext context) {
	  vertx.close(context.asyncAssertSuccess());
	}
	
	/**
	 * Test for machine-message endpoint
	 * @param context
	 */
	@Test
	public void testMessageEndpoint(TestContext context){
		Async async = context.async();
		  vertx.createHttpClient().post(port, "localhost", "/rest/v2/message?validate=true", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    response.bodyHandler(body -> {
		      async.complete();
		    });
		  })
	      .putHeader("content-type", "application/json")
	      .putHeader("content-length", String.valueOf(TEST_MESSAGE.length()))
		  .write(TEST_MESSAGE)
	      .end();
	}
	
	/**
	 * Test for measurement-message endpoint
	 * @param context
	 */
	@Test
	public void testMeasurementEndpoint(TestContext context){
		Async async = context.async();
		  vertx.createHttpClient().post(port, "localhost", "/rest/v2/message?validate=true", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    response.bodyHandler(body -> {
		      async.complete();
		    });
		  })
	      .putHeader("content-type", "application/json")
	      .putHeader("content-length", String.valueOf(TEST_MESSAGE.length()))
		  .write(TEST_MESSAGE)
	      .end();
	}
	
	/**
	 * Test for process-message endpoint
	 * @param context
	 */
	@Test
	public void testProcessEndpoint(TestContext context){
		Async async = context.async();
		  vertx.createHttpClient().post(port, "localhost", "/rest/v2/message?validate=true", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    response.bodyHandler(body -> {
		      async.complete();
		    });
		  })
	      .putHeader("content-type", "application/json")
	      .putHeader("content-length", String.valueOf(TEST_MESSAGE.length()))
		  .write(TEST_MESSAGE)
	      .end();
	}
	
	/**
	 * Test for influxDB activation endpoint
	 * @param context
	 */
	@Test
	public void testInfluxDbActivation(TestContext context){
		Async async = context.async();
		  vertx.createHttpClient().post(port, "localhost", "/influxdb?active=true", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    response.bodyHandler(body -> {
		    	 context.assertEquals(body.toString(), "InfluxDB activated");
		      async.complete();
		    });
		  })
		  .putHeader("content-type", "application/json")
	      .end();
	}
	
	/**
	 * Test for global GET-endpoint
	 * @param context
	 */
	@Test
	public void testGet(TestContext context){
		Async async = context.async();
		  vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
		    context.assertEquals(response.statusCode(), 200);
		    context.assertEquals(response.getHeader("content-type"), "text/html");
		    response.bodyHandler(body -> {
		    	 context.assertEquals(body.toString(), "<h1>This is the service for validating PPMP-messages</h1>");		    	
		      async.complete();
		    });
		  });
	}
	
}
