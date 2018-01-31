/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.web;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import server.MainVerticle;

@RunWith( VertxUnitRunner.class )
public class RestEndpointInfluxTest {
   private static final String MACHINE_MESSAGE_VALID = "server/messages/machine_message_valid.json";
   private static final String MACHINE_MESSAGE_INVALID = "server/messages/machine_message_invalid.json";
   private static final String MEASUREMENT_MESSAGE_VALID = "server/messages/measurement_message_valid.json";
   private static final String MEASUREMENT_MESSAGE_INVALID = "server/messages/measurement_message_invalid.json";
   private static final String PROCESS_MESSAGE_VALID = "server/messages/process_message_valid.json";
   private static final String PROCESS_MESSAGE_INVALID = "server/messages/process_message_invalid.json";

   private static final int PORT = 8080;
   private static final String APPLICATION_URL = "http://localhost" + ":" + PORT;
   private static final int WIREMOCK_PORT = 9090;
   private static final String WIREMOCK_URL = "http://localhost:" + WIREMOCK_PORT;

   private static final String PPMP_REST_PATH = "/rest/v2/";
   private static final String PPMP_REST_VALIDATE_VALIDATE = "/rest/v2/validate";
   private Vertx vertx;

   @Rule
   public final WireMockRule wireMockRule = new WireMockRule( WIREMOCK_PORT );

   @Before
   public void setUp( TestContext context ) throws IOException {
      RestAssured.baseURI = APPLICATION_URL;
      createInfluxDbCreateDatabaseStubs();
      vertx = Vertx.vertx();
      DeploymentOptions options = new DeploymentOptions().setConfig( createConfig() );
      vertx.deployVerticle( MainVerticle.class.getName(), options, context.asyncAssertSuccess() );
   }

   @After
   public void after() {
      vertx.close();
   }

   @Test
   public void test_send_valid_ppmp_machine_message_expect_200() {
      String validMessage = FileUtils.readFile( MACHINE_MESSAGE_VALID );
      wireMockRule.stubFor( WireMock.post( "/write?u=root&p=root&db=Messages&precision=n&consistency=all" )
                                    .withRequestBody( WireMock.equalTo(
                                          "ppmp_messages,code=33-02,deviceId=2ca5158b-8350-4592-bff9-755194497d4e "
                                                + "description=\"Disk size has reached limit. Unable to write log files.\","
                                                + "severity=\"HIGH\",title=\"Disk size limit reached\",type=\"TECHNICAL_INFO\" 1022743810125000000\n" ) )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 1,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/write?u=root&p=root&db=Messages&precision=n&consistency=all" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_machine_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, MACHINE_MESSAGE_INVALID,
            "{\"messages[1].ts\":\"is missing but it is required\"}" );
   }

   @Test
   public void test_send_valid_ppmp_measurement_message_expect_200() {
      String validMessage = FileUtils.readFile( MEASUREMENT_MESSAGE_VALID );
      wireMockRule.stubFor( WireMock.post( "/write?u=root&p=root&db=Measurements&precision=n&consistency=all" )
                                    .withRequestBody( WireMock.equalTo(
                                          "ppmp_measurements,deviceId=a4927dad-58d4-4580-b460-79cefd56775b,measurementPoint=force value=26.0 1022743810123000000\n"
                                                + "ppmp_measurements,deviceId=a4927dad-58d4-4580-b460-79cefd56775b,measurementPoint=force value=23.0 1022743810146000000\n"
                                                + "ppmp_measurements,deviceId=a4927dad-58d4-4580-b460-79cefd56775b,measurementPoint=pressure value=52.4 1022743810123000000\n"
                                                + "ppmp_measurements,deviceId=a4927dad-58d4-4580-b460-79cefd56775b,measurementPoint=pressure value=46.32 1022743810146000000\n" ) )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 1,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/write?u=root&p=root&db=Measurements&precision=n&consistency=all" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_measurement_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, MEASUREMENT_MESSAGE_INVALID,
            "{\"measurements[0].series.$_time\":\"is missing but it is required\",\"measurements[0].series\":\"should have a minimum of 2 properties\"}" );
   }

   @Test
   public void test_send_valid_ppmp_process_message_expect_200() {
      String validMessage = FileUtils.readFile( PROCESS_MESSAGE_VALID );
      wireMockRule.stubFor( WireMock.post( "/write?u=root&p=root&db=Processes&precision=n&consistency=all" )
                                    .withRequestBody( WireMock.containing(
                                          "ppmp_processes,programm=Programm\\ 1 payload=" ) )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 1,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/write?u=root&p=root&db=Processes&precision=n&consistency=all" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_process_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, PROCESS_MESSAGE_INVALID,
            "{\"measurements\":\"is missing but it is required\"}" );
   }

   @Test
   public void test_valid_message_should_return_200() {
      String invalidMessage = FileUtils.readFile( PROCESS_MESSAGE_VALID );
      given().body( invalidMessage ).post( PPMP_REST_VALIDATE_VALIDATE )
             .then().statusCode( 200 )
             .body( equalTo( "Ppmp Message of type 'PROCESS' is valid" ) );
   }


   private void testInvalidMessage( String url, String testMessageFileName, String failureMessage ) {
      String invalidMessage = FileUtils.readFile( testMessageFileName );
      given().body( invalidMessage ).post( url )
             .then().statusCode( 400 )
             .body( containsString( failureMessage ) );
   }

   private void createInfluxDbCreateDatabaseStubs() {
      wireMockRule.stubFor( WireMock.get( "/ping" )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );
      wireMockRule.stubFor( WireMock.post( "/query?u=root&p=root&q=CREATE+DATABASE+%22Messages%22" )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );
      wireMockRule.stubFor( WireMock.post( "/query?u=root&p=root&q=CREATE+DATABASE+%22Measurements%22" )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );
      wireMockRule.stubFor( WireMock.post( "/query?u=root&p=root&q=CREATE+DATABASE+%22Processes%22" )
                                    .willReturn( WireMock.aResponse().withBody( "{}" ).withStatus( 200 ) ) );
   }

   private JsonObject createConfig() {
      return new JsonObject()
            .put( "http.port", PORT )
            .put( "influxDb.url", WIREMOCK_URL )
            .put( "influxDb.user", "root" )
            .put( "influxDb.password", "root" );
   }

}
