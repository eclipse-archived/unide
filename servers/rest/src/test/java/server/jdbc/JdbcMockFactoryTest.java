/*
 * Copyright (c) 2018 Bosch Software Innovations GmbH. All rights reserved.
 */

package server.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class JdbcMockFactoryTest {

   @Rule
   public WireMockRule wireMockRule = new WireMockRule( 8080 );

   @Test
   public void execute_update_statement_should_return_valid_response() throws SQLException {
      int expectedUpdateResult = 21;
      wireMockRule.stubFor( WireMock.post( "/sqlStub" )
                                    .withRequestBody( WireMock
                                          .equalTo( "INSERT INTO TEST(col1, col2, col3, col4) VALUES(?,?,?,?)" ) )
                                    .withHeader( "1", WireMock.equalTo( "UpdateValue1" ) )
                                    .withHeader( "2", WireMock.equalTo( "UpdateValue2" ) )
                                    .withHeader( "3", WireMock.equalTo( "UpdateValue3" ) )
                                    .withHeader( "4", WireMock.equalTo( "10.0" ) )
                                    .willReturn( WireMock.aResponse().withBody( String.valueOf( expectedUpdateResult ) )
                                                         .withStatus( 200 ) )
      );

      Connection mockConnection = JdbcMockFactory.getMockConnection( "http://localhost:8080/sqlStub" );
      PreparedStatement preparedStatement = mockConnection
            .prepareStatement( "INSERT INTO TEST(col1, col2, col3, col4) VALUES(?,?,?,?)" );
      preparedStatement.setString( 1, "UpdateValue1" );
      preparedStatement.setString( 2, "UpdateValue2" );
      preparedStatement.setString( 3, "UpdateValue3" );
      preparedStatement.setDouble( 4, 10 );
      int updateResult = preparedStatement.executeUpdate();
      Assert.assertEquals( expectedUpdateResult, updateResult );
   }

}
