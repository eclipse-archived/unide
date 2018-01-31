/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.web;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import server.DependencyProvider;
import server.MainVerticle;
import server.jdbc.JdbcMockFactory;
import server.receiver.Receiver;
import server.receiver.ReceiverException;

@RunWith( VertxUnitRunner.class )
public class RestEndpointPostgresTest {
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
   private Vertx vertx;

   @Rule
   public final WireMockRule wireMockRule = new WireMockRule( WIREMOCK_PORT );

   @Before
   public void setUp( TestContext context ) throws IOException, ReceiverException {
      RestAssured.baseURI = APPLICATION_URL;

      vertx = Vertx.vertx();
      DeploymentOptions options = new DeploymentOptions().setConfig( createConfig() );

      DependencyProvider dependencyProvider = Mockito.spy( new DependencyProvider( createConfig() ) );
      Mockito.when( dependencyProvider.getConnection() )
             .thenReturn( () -> JdbcMockFactory.getMockConnection( WIREMOCK_URL + "/sqlStub" ) );

      Receiver postgresReceiver = Mockito.spy( dependencyProvider.getReceiver() );
      Mockito.when( dependencyProvider.getReceiver() ).thenReturn( postgresReceiver );
      Mockito.doAnswer( ( Void ) -> null )
             .when( postgresReceiver ).init();

      vertx.deployVerticle( new MainVerticle( dependencyProvider ), options,
            context.asyncAssertSuccess() );
   }

   @After
   public void after() {
      vertx.close();
   }

   @Test
   public void test_send_valid_ppmp_machine_message_expect_200() {
      String validMessage = FileUtils.readFile( MACHINE_MESSAGE_VALID );
      wireMockRule.stubFor( WireMock.post( "/sqlStub" )
                                    .withRequestBody( WireMock.equalTo(
                                          "INSERT INTO ppmp_messages(time, deviceid, code, severity, title, description, hint, type) VALUES(?,?,?,?,?,?,?,?)" ) )
                                    .withHeader( "1", WireMock.equalTo( "2002-05-30 09:30:10.125" ) )
                                    .withHeader( "2", WireMock.equalTo( "2ca5158b-8350-4592-bff9-755194497d4e" ) )
                                    .withHeader( "3", WireMock.equalTo( "33-02" ) )
                                    .withHeader( "4", WireMock.equalTo( "HIGH" ) )
                                    .withHeader( "5", WireMock.equalTo( "Disk size limit reached" ) )
                                    .withHeader( "6", WireMock
                                          .equalTo( "Disk size has reached limit. Unable to write log files." ) )
                                    .withHeader( "7", WireMock.equalTo( "null" ) )
                                    .withHeader( "8", WireMock.equalTo( "TECHNICAL_INFO" ) )
                                    .willReturn( WireMock.aResponse().withBody( "1" ).withStatus( 200 ) ) );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 1,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/sqlStub" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_machine_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, MACHINE_MESSAGE_INVALID,
            "{\"messages[1].ts\":\"is missing but it is required\"}" );
   }

   @Test
   public void test_send_valid_ppmp_measurement_message_expect_200() {
      String validMessage = FileUtils.readFile( MEASUREMENT_MESSAGE_VALID );
      createPpmpMeasurementMessageStub( "2002-05-30 09:30:10.123", "a4927dad-58d4-4580-b460-79cefd56775b",
            "force", "26.0" );
      createPpmpMeasurementMessageStub( "2002-05-30 09:30:10.146", "a4927dad-58d4-4580-b460-79cefd56775b",
            "force", "23.0" );
      createPpmpMeasurementMessageStub( "2002-05-30 09:30:10.123", "a4927dad-58d4-4580-b460-79cefd56775b",
            "pressure", "52.4" );
      createPpmpMeasurementMessageStub( "2002-05-30 09:30:10.146", "a4927dad-58d4-4580-b460-79cefd56775b",
            "pressure", "46.32" );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 4,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/sqlStub" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_measurement_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, MEASUREMENT_MESSAGE_INVALID,
            "{\"measurements[0].series.$_time\":\"is missing but it is required\",\"measurements[0].series\":\"should have a minimum of 2 properties\"}" );
   }

   @Test
   public void test_send_valid_ppmp_process_message_expect_200() {
      String validMessage = FileUtils.readFile( PROCESS_MESSAGE_VALID );
      wireMockRule.stubFor( WireMock.post( "/sqlStub" )
                                    .withRequestBody( WireMock.equalTo(
                                          "INSERT INTO ppmp_processes(time, deviceid, programname, payload) VALUES(?,?,?,to_json(?::json))" ) )
                                    .withHeader( "1", WireMock.equalTo( "2002-05-30 09:30:10.123" ) )
                                    .withHeader( "2", WireMock.equalTo( "a4927dad-58d4-4580-b460-79cefd56775b" ) )
                                    .withHeader( "3", WireMock.equalTo( "Programm 1" ) )
                                    .withHeader( "4", WireMock.containing( "content-spec" ) )
                                    .willReturn( WireMock.aResponse().withBody( "0" ).withStatus( 200 ) ) );

      given().body( validMessage ).post( PPMP_REST_PATH )
             .then().statusCode( 200 )
             .body( containsString( "" ) );

      WireMock.verify( 1,
            WireMock
                  .postRequestedFor(
                        WireMock.urlEqualTo( "/sqlStub" ) ) );
   }

   @Test
   public void test_send_invalid_ppmp_process_message_expect_400() {
      testInvalidMessage( PPMP_REST_PATH, PROCESS_MESSAGE_INVALID,
            "{\"measurements\":\"is missing but it is required\"}" );
   }

   private void createPpmpMeasurementMessageStub( String time, String deviceId, String measurementPoint,
         String value ) {
      wireMockRule.stubFor( WireMock.post( "/sqlStub" )
                                    .withRequestBody( WireMock.equalTo(
                                          "INSERT INTO ppmp_measurements(time, deviceid, measurementpoint, value) VALUES(?,?,?,?)" ) )
                                    .withHeader( "1", WireMock.equalTo( time ) )
                                    .withHeader( "2", WireMock.equalTo( deviceId ) )
                                    .withHeader( "3", WireMock.equalTo( measurementPoint ) )
                                    .withHeader( "4", WireMock.equalTo( value ) )
                                    .willReturn( WireMock.aResponse().withBody( "2" ).withStatus( 200 ) ) );
   }

   private void testInvalidMessage( String url, String testMessageFileName, String failureMessage ) {
      String invalidMessage = FileUtils.readFile( testMessageFileName );
      given().body( invalidMessage ).post( url )
             .then().statusCode( 400 )
             .body( containsString( failureMessage ) );
   }

   private JsonObject createConfig() {
      return new JsonObject()
            .put( "http.port", PORT )
            .put( "persistence.system", "postgres" );
   }

}
