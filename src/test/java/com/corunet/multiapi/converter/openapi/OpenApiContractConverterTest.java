/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.corunet.multiapi.converter.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.corunet.multiapi.converter.MultiApiContractConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Body;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.ResponseBodyMatchers;

@Slf4j
class OpenApiContractConverterTest {

  private final MultiApiContractConverter multiApiContractConverter = new MultiApiContractConverter();

  @Test
  @DisplayName("OpenApi: Testing the method that checks if the yaml is incorrect")
  void isAcceptedFalse() {
    final File file = new File("src/test/resources/openapi/testFalse.yml");
    final Boolean isAccepted = multiApiContractConverter.isAccepted(file);
    assertThat(isAccepted).isFalse();
  }

  @Test
  @DisplayName("OpenApi: Testing the method that checks if the yaml is correct")
  void isAcceptedTrue() {
    final File file = new File("src/test/resources/openapi/testCompleteApi.yml");
    Boolean isAccepted = multiApiContractConverter.isAccepted(file);
    assertThat(isAccepted).isTrue();
  }

  @Test
  @DisplayName("OpenApi: Check if a contract is returned")
  void convertFromTest() {
    final File file = new File("src/test/resources/openapi/testCompleteApi.yml");
    final Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    final ArrayList<Contract> contractList = new ArrayList<>(contracts);
    assertThat(contractList).hasSize(6);
    assertThat(contractList.get(0).getResponse()).isNotNull();
    assertThat(contractList.get(0).getRequest()).isNotNull();
    assertThat(contractList.get(0).getName()).isInstanceOf(String.class);
    assertThat(contractList.get(0).getDescription()).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("OpenApi: Check if RequestHeaders are being processed okay")
  void testRequestHeaders() {
    final File file = new File("src/test/resources/openapi/testRequestHeaders.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    final Contract contract = contractList.get(0);
    final Header header = contract.getRequest().getHeaders().getEntries().stream().iterator().next();
    assertThat(header.getName()).isEqualTo("Content-Type");
    assertThat(header.getClientValue()).isEqualTo("application/json");
    assertThat(header.getServerValue()).isEqualTo("application/json");
  }

  @Test
  @DisplayName("OpenApi: Check if QueryParameters are being processed okay")
  void testRequestQueryParameters() {
    final File file = new File("src/test/resources/openapi/testRequestQueryParameters.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    final QueryParameters queryParameters = contract.getRequest().getUrlPath().getQueryParameters();
    final QueryParameter parameter = queryParameters.getParameters().get(0);
    assertThat(parameter.getName()).isEqualTo("gameId");
    assertThat(parameter.getClientValue()).isInstanceOf(Pattern.class);
    Assertions.assertTrue("([1-9]\\d*)".equalsIgnoreCase(parameter.getClientValue().toString()));
  }

  @Test
  @DisplayName("OpenApi: Check if a simple Request Body is being processed okay")
  void testRequestBody() {
    final File file = new File("src/test/resources/openapi/testRequestBody.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    final Body body = contract.getRequest().getBody();
    final HashMap<String, Object> bodyServerValueMap = (HashMap<String, Object>) body.getServerValue();
    assertThat(bodyServerValueMap).isNotEmpty();
    assertThat(bodyServerValueMap.get("gameId")).isInstanceOf(Integer.class);
    assertThat(bodyServerValueMap.get("playerName")).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("OpenApi: Check if a simple Request BodyMatcher is being processed okay")
  void testRequestBodyMatcher() {
    File file = new File("src/test/resources/openapi/testRequestBody.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    final BodyMatchers bodyMatchers = contract.getRequest().getBodyMatchers();
    final List<BodyMatcher> bodyMatcherList = bodyMatchers.matchers();
    for (BodyMatcher bodyMatcher : bodyMatcherList) {
      assertThat(bodyMatcher.path()).isInstanceOf(String.class);
      assertThat(bodyMatcher.matchingType()).isInstanceOf(MatchingType.class);
    }
  }

  @Test
  @DisplayName("OpenApi: Check if the enum logic is being processed okay")
  void testEnums() {
    final File file = new File("src/test/resources/openapi/testEnums.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    final Body body = contract.getResponse().getBody();
    final String[] enumValues = {"hola", "adios"};
    HashMap<String, Object> bodyServerValueMap = (HashMap<String, Object>) body.getServerValue();
    assertThat(bodyServerValueMap).isNotEmpty();
    final String name = (String) bodyServerValueMap.get("name");
    assertThat(name)
        .isInstanceOf(String.class)
        .isIn(enumValues);
  }

  @Test
  @DisplayName("OpenApi: Check if complex objects are being processed okay")
  void testComplexObjects() {
    final File file = new File("src/test/resources/openapi/testComplexObjects.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    assertThat(contract)
        .isNotNull()
        .isInstanceOf(Contract.class);
    assertThat(contract.getRequest()).isNotNull();
    assertThat(contract.getResponse()).isNotNull();
    Body body = contract.getResponse().getBody();
    HashMap<String, Object> bodyServerValueMap = (HashMap<String, Object>) body.getServerValue();
    assertThat(bodyServerValueMap)
        .isNotNull()
        .containsKey("name")
        .hasSize(2);
    final HashMap<String, Object> nameSubMap = (HashMap<String, Object>) bodyServerValueMap.get("name");
    assertThat(nameSubMap)
        .containsKey("lastname")
        .hasSize(2);
    assertThat(nameSubMap.get("firstname")).isInstanceOf(String.class);
    assertThat(nameSubMap.get("lastname")).isInstanceOf(String.class);

  }

  @Test
  @DisplayName("OpenApi: Check if Arrays are being processed okay")
  void testArrays() {
    final File file = new File("src/test/resources/openapi/testArrays.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    assertThat(contract).isNotNull();
    assertThat(contract.getRequest()).isNotNull();
    assertThat(contract.getResponse()).isNotNull();
    final Map<String, Object> bodyServerValueMap = (HashMap<String, Object>) contract.getResponse().getBody().getServerValue();
    assertThat(bodyServerValueMap)
        .containsKey("name")
        .containsKey("address")
        .isNotNull();
    final List<HashMap<String, String>> nameSubList = (ArrayList<HashMap<String, String>>) bodyServerValueMap.get("name");
    final Map<String, String> lastNameMap = nameSubList.get(0);
    assertThat(lastNameMap).containsKey("lastname");
    assertThat(lastNameMap.get("lastname")).isInstanceOf(String.class);
    final List<ArrayList<String>> addressSubList = (ArrayList<ArrayList<String>>) bodyServerValueMap.get("address");
    assertThat(addressSubList.get(0)).isInstanceOf(ArrayList.class);
    assertThat(addressSubList.get(0).get(0)).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("OpenApi: Check if Refs are being processed okay")
  void testRef() {
    final File file = new File("src/test/resources/openapi/testRefs.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    assertThat(contract).isNotNull();
    assertThat(contract.getRequest()).isNotNull();
    assertThat(contract.getResponse()).isNotNull();
    Map<String, Object> bodyServerValueMap = (HashMap<String, Object>) contract.getResponse().getBody().getServerValue();
    assertThat(bodyServerValueMap)
        .containsKey("player");
    final Map<String, Object> playerMap = (HashMap<String, Object>) bodyServerValueMap.get("player");
    assertThat(playerMap)
        .containsKey("name");
    final Map<String, Object> nameMap = (Map<String, Object>) playerMap.get("name");
    assertThat(nameMap)
        .containsKey("firstname")
        .containsKey("lastname");
    assertThat(nameMap.get("firstname")).isInstanceOf(String.class);
    assertThat(nameMap.get("lastname")).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("OpenApi: Check if oneOfs are being processed okay")
  void testOneOfsAndAnyOfs() {

    final List<File> fileList = new ArrayList<>();
    fileList.add(new File("src/test/resources/openapi/testOneOfs.yml"));
    fileList.add(new File("src/test/resources/openapi/testAnyOfs.yml"));
    for (File file : fileList) {
      Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
      ArrayList<Contract> contractList = new ArrayList<>(contracts);
      Contract contract = contractList.get(0);
      assertThat(contract).isNotNull();
      assertThat(contract.getResponse()).isNotNull();
      final List<String> assertKeys = new ArrayList<>();
      Map<String, Object> bodyServerValueMap = (HashMap<String, Object>) contract.getResponse().getBody().getServerValue();
      bodyServerValueMap.forEach((key, value) ->
                                 {
                                   assertKeys.add(key);
                                 }
      );
      assertThat(assertKeys).containsAnyOf("gameId", "gameName", "roomId", "newGameId", "playerName");
    }
  }

  @Test
  @DisplayName("OpenApi: Check if AllOfs are being processed okay")
  void testAllOfs() {
    final File file = new File("src/test/resources/openapi/testAllOfs.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    assertThat(contract).isNotNull();
    assertThat(contract.getResponse()).isNotNull();
    List<String> assertKeys = new ArrayList<>();
    Map<String, Object> bodyServerValueMap = (HashMap<String, Object>) contract.getResponse().getBody().getServerValue();
    bodyServerValueMap.forEach((key, value) ->
                               {
                                 assertKeys.add(key);
                               }
    );
    assertThat(assertKeys).containsExactlyInAnyOrder("gameId", "gameName", "roomId", "newGameId", "playerName");
  }

  @Test
  @DisplayName("OpenApi: Check that BasicSchemas are being processed okay")
  void testBasicSchema() {
    final File file = new File("src/test/resources/openapi/testBasicSchema.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    assertThat(contract).isNotNull();
    final Response response = contract.getResponse();
    assertThat(response).isNotNull();
    assertThat(response.getBody().getServerValue()).isNotNull();
    assertThat(response.getBody().getServerValue().toString()).hasToString("[a-zA-Z0-9]+");
  }

  @Test
  @DisplayName("OpenApi: Check that Basic Schemas and $refs processing works well with both. ")
  void testBasicObjectAndRef() {
    final File file = new File("src/test/resources/openapi/testBasicObjectAndRef.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    Response response = contract.getResponse();
    final Map<String, Object> bodyServerValueMap = (Map<String, Object>) response.getBody().getServerValue();
    final Map<String, Object> messageMap = (Map<String, Object>) bodyServerValueMap.get("message");
    final ResponseBodyMatchers responseBodyMatchers = response.getBodyMatchers();
    final List<BodyMatcher> bodyMatchers = responseBodyMatchers.matchers();
    assertThat(contract).isNotNull();
    assertThat(bodyServerValueMap.get("code")).isInstanceOf(Integer.class);
    assertThat(bodyMatchers.get(0).path()).isEqualTo("code");
    assertThat(bodyMatchers.get(0).value().toString()).hasToString("([1-9]\\d*)");
    assertThat(bodyMatchers.get(1).path()).isEqualTo("message.description");
    assertThat(bodyMatchers.get(1).value().toString()).hasToString("[a-zA-Z0-9]+");
    assertThat(messageMap).hasSize(1);
    assertThat(messageMap.get("description")).isInstanceOf(String.class);
  }

  @Test
  @DisplayName("OpenApi: Check that Examples are being processed okay")
  void testExamples() {
    final File file = new File("src/test/resources/openapi/testExamples.yml");
    Collection<Contract> contracts = multiApiContractConverter.convertFrom(file);
    ArrayList<Contract> contractList = new ArrayList<>(contracts);
    Contract contract = contractList.get(0);
    QueryParameters queryParameters = contract.getRequest().getUrlPath().getQueryParameters();
    QueryParameter parameter = queryParameters.getParameters().get(0);
    assertThat(parameter.getName()).isEqualTo("gameId");
    final MatchingStrategy matchingStrategy = (MatchingStrategy) parameter.getServerValue();
    assertThat(matchingStrategy.getType().toString()).hasToString("EQUAL_TO");
    assertThat(matchingStrategy.getServerValue()).isEqualTo(1);
    assertThat(matchingStrategy.getClientValue()).isEqualTo(1);
    final HashMap<String, Object> serverValueMap = (HashMap<String, Object>) contract.getResponse().getBody().getServerValue();
    assertThat(serverValueMap)
        .containsEntry("rooms", 1)
        .containsEntry("gameName", "hangman");
  }
}