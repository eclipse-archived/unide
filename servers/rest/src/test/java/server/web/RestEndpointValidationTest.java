/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.web;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import server.MainVerticle;

/**
 * Unit test for validation Endpoint.
 *
 * This test ensures that the validation endpoint is still working when persistence is disabled.
 */
@RunWith( VertxUnitRunner.class )
public class RestEndpointValidationTest {

   private static final String PROCESS_MESSAGE_VALID = "server/messages/process_message_valid.json";
   private static final String PROCESS_MESSAGE_INVALID = "server/messages/process_message_invalid.json";

   private static final int PORT = 8080;
   private static final String APPLICATION_URL = "http://localhost" + ":" + PORT;
   private static final int WIREMOCK_PORT = 9090;

   private static final String PPMP_REST_VALIDATE_VALIDATE = "/rest/v2/validate";
   private Vertx vertx;

   @Rule
   public final WireMockRule wireMockRule = new WireMockRule( WIREMOCK_PORT );

   @Before
   public void setUp( TestContext context ) throws IOException {
      RestAssured.baseURI = APPLICATION_URL;
      vertx = Vertx.vertx();
      DeploymentOptions options = new DeploymentOptions()
            .setConfig( new JsonObject().put( "http.port", PORT )
                                        .put( "enable.persistence", false ) );
      vertx.deployVerticle( MainVerticle.class.getName(), options, context.asyncAssertSuccess() );
   }

   @After
   public void after() {
      vertx.close();
   }

   @Test
   public void test_valid_message_should_return_200() {
      String invalidMessage = FileUtils.readFile( PROCESS_MESSAGE_VALID );
      given().body( invalidMessage ).post( PPMP_REST_VALIDATE_VALIDATE )
             .then().statusCode( 200 )
             .body( equalTo( "Ppmp Message of type 'PROCESS' is valid" ) );
      assertThat( wireMockRule.getServeEvents().getRequests(), empty() );
   }

   @Test
   public void test_valid_message_should_return_400() {
      String invalidMessage = FileUtils.readFile( PROCESS_MESSAGE_INVALID );
      given().body( invalidMessage ).post( PPMP_REST_VALIDATE_VALIDATE )
             .then().statusCode( 400 )
             .body( equalTo(
                   "{\"measurements\":\"is missing but it is required\"}" ) );
      assertThat( wireMockRule.getServeEvents().getRequests(), empty() );
   }

}
