/**
 * copyright 2024 RS TECHNOLOGIES SP. Z.O.O.
 */
package Tests;

import java.util.HashMap;
import java.util.Map;
import helpers.ApiHelpers;
import helpers.logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Tests_Part_02_Setup_Network_via_API {
    /**
     * Method makes POST REQUEST Login api call to: /api/login
     * using hardcoded credentials ( to be hashed and moved to separate file in future commits)
     *
     * @return LoginResponse which is Map with 3 keys (strings) and 3 string values. the keys are following:
     * response - response body
     * responseCookie - whole cookie from response
     * loginCookie - essential information that are used in potential farther used  POST request in next steps of the given test
     */
    public Map<String, String> Login_viaAPI() {
        // POST Login
        String url = "http://127.0.0.1:8080/api/login";
        String requestType = "POST";
        String jsonString = "{\"username\": \"admin\",\"password\": \"password\"}"; //password should be MD5 or encrypted and moved to separate file

        Map<String, String> response = ApiHelpers.makeApiRESTCall(url, requestType, null, jsonString,true);
        logger.log("response is: " + response.get("response"));
        logger.log("response cookie: " + response.get("responseCookie"));

        String csrf_token = logger.ExtractStringREGEXP(response.get("responseCookie"), "csrf_token=([^;]+)");
        String unifises = logger.ExtractStringREGEXP(response.get("responseCookie"), "unifises=([^;]+)");
        String loginCookie = csrf_token + ";" + unifises + ";";
        logger.log("loginCookie = " + loginCookie);

        Map<String,String> LoginResponse = new HashMap<String,String>(){{
            put("response", response.get("response"));
            put("responseCookie", response.get("responseCookie"));
            put("loginCookie", loginCookie);
        }};

        return  LoginResponse;
    }

    /**
     * This method extracts from Json (preleminary defined structure)
     *
     * @param firstKey - first JSON key/section
     * @param secondKey   -  second JSON key/section
     * @param inputString - inputJson as String
     * @return keyvalue for secondKey
     *
     * To do: this methd might be improved for more generic JSON response parsing - but now it does it's job
     */
    public String extractFromJson(String firstKey, String secondKey, String inputString){
        // Get user id - > extract userid from the response
        JSONObject jsonObject = new JSONObject(inputString);
        // Access the "data" array
        JSONArray dataArray = jsonObject.getJSONArray(firstKey);
        // Extract the first object in the "data" array
        JSONObject dataObject = dataArray.getJSONObject(0);
        // Get the value of "_id"
        String extractedKeyValue = dataObject.getString(secondKey);
        // Print the extracted value
        logger.log(secondKey +" = " + extractedKeyValue);
        return extractedKeyValue;

    }

    @Test
    /**
     *
     * This test checks if it is possible to fetch admins using cmd "get-admin" - additional test
     * it was developped during reverse engineering of finding cmd "add-default-admin"
     * Assumption:
     * there are admin users created on tested environment
     *
     * Steps:
     * 1. Login using API call
     * 2. using POST API call execute command "get-admins"
     * 3. check if user admin is created
     */
    public void Test_01_POST_GET_ADMINS() {
        logger.log("Start of Test: Test_01_POST_GET_ADMINS");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();

        // 2. using POST API call execute command "get-admins"
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        String url = "http://127.0.0.1:8080/api/s/default/cmd/sitemgr"; // URL From doucmentation api/s/default/cmd/sitemgr (do not works)

        String requestType = "POST";

        String jsonString = "{\"cmd\":\"get-admins\", \"username\": \"admin\", \"password\": \"password\"}";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString, false);

        // 3. check if user admin is created
        String responseOutput = response.get("response");
        logger.log("responseOutput= " + responseOutput);
        Assert.assertEquals(responseOutput.contains("\"name\":\"admin\","),true);

        logger.log("End of Test: Test_01_POST_GET_ADMINS");
    }


    @Test
    /**
     * WARN: task says that is should use following call
     *
     * POST /api/cmd/sitemgr '{cmd: "add-default-admin", name: "admin", email: "network-admin@gmail.com", x_password: "password"}' - to create local admin
     * BUT IT IS NOT WORKING - BUG against software or documentation should be created
     *
     * SO:
     * This test checks if admin-user with can be created and deleted.
     *
     * Assumption:
     * user created in this test "TestUser" is not existing on system
     *
     * Steps:
     * 1. Login using API call
     * 2. create new user "TestUser" with some default password
     * 3. extract userID for freshly created user
     * 4. delete created user
     * 5. Check if deleted user is not existing
     */
    public void Test_02_POST_CREATE_AND_DELETE_ADMIN() {
        logger.log("Start of Test: Test_01_POST_CREATE_AND_DELETE_ADMIN");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();

        // 2. create new user "TestUser" with some default password
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        String url = "http://127.0.0.1:8080/api/s/default/cmd/sitemgr"; // URL From doucmentation api/s/default/cmd/sitemgr (do not works)
        String requestType = "POST";
        String jsonString = "{\"cmd\":\"create-admin\",\"name\":\"TestUser\",\"requires_new_password\":false,\"role\":\"readonly\",\"x_password\":\"testUserpassword123\",\"permissions\":[]}";
        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString, false);

        String responseOutput = response.get("response");
        logger.log("responseOutput= " + responseOutput);
        Assert.assertEquals(responseOutput.contains("api.err.NameExisted"),false);
        Assert.assertEquals(responseOutput.contains("\"name\":\"TestUser\","),true);

        // 3. extract userID for freshly created user
        String userId = extractFromJson("data","_id",responseOutput);

        logger.log("_id: " + userId);

        // 4. delete created user
        url = "http://127.0.0.1:8080/api/s/default/cmd/sitemgr";
        jsonString = "{\"admin\":\""+userId +"\",\"cmd\":\"revoke-admin\"}";
        logger.log("jsonString = " +jsonString);

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString, false);

        responseOutput = response.get("response");
        Assert.assertEquals(responseOutput.contains("ok"),true);

        // 5. Check if deleted user is not existing
        url = "http://127.0.0.1:8080/api/s/default/cmd/sitemgr"; // URL From doucmentation api/s/default/cmd/sitemgr (do not works)
        requestType = "POST";
        jsonString = "{\"cmd\":\"get-admins\", \"username\": \"admin\", \"password\": \"password\"}";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString, false);

        responseOutput = response.get("response");

        logger.log("responseOutput= " + responseOutput);
        logger.log("Checking if user created at the begining of the test has been deleted");
        Assert.assertEquals(responseOutput.contains("\"name\":\"TestUser\","),false);

        logger.log("End of Test: Test_01_POST_CREATE_AND_DELETE_ADMIN");
    }


    @Test
    /** this test is using api call /api/s/default/set/setting/super_identity
     * and updates application name to: UniFi Network
     * Steps:
     * 1. Login using API call
     * 2. using POST API call update application name to: UniFi Network
     * 3. validate if response contains updated application name
     */
    public void Test_03_POST_SET_APPLICATION_NAME() {
        logger.log("Start of Test: Test_02_POST_SET_APPLICATION_NAME");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. using POST API call update application name to: UniFi Network
        String url = "http://127.0.0.1:8080/api/s/default/set/setting/super_identity";
        String requestType = "POST";
        String jsonString = "{\"name\":\"UniFi Network\"}";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);

        // 3. validate if response contains updated application name
        String responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);
        Assert.assertEquals(responseOutput.contains("UniFi Network"),true);

        logger.log("Start of Test: Test_02_POST_SET_APPLICATION_NAME");
    }


    @Test
    /**
     * This tests ability of change of "Country/Region" is possible using API POST Call
     * Steps
     * 1. Login using API call
     * 2. change country code using api call
     * 3. check if country settings change was effective
     * 4. roll-back change to default environment value = Poland - code 616
     * 5. check if country settings roll-back was effective
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_04_POST_SET_COUNTRY() {
        logger.log("Start of Test: Test_04_POST_SET_COUNTRY");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. change country code using api call
        String url = "http://127.0.0.1:8080/api/s/default/set/setting/country";
        String requestType = "POST";
        String jsonString = "{\"code\": \"840\"}";
        logger.log("cookieGETrequest = " + cookieGETrequest);

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);
        String responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);

        // 3. check if country settings change was effective
        String expectedCountry = "\"code\":\"840\"";
        url = "http://127.0.0.1:8080/api/s/default/get/setting/country";
        requestType = "GET";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, null,false);
        responseOutput = response.get("response");
        Assert.assertEquals(responseOutput.contains(expectedCountry),true);

        // 4. roll-back change to default environment value = Poland - code 616
        url = "http://127.0.0.1:8080/api/s/default/set/setting/country";
        requestType = "POST";
        jsonString = "{\"code\": \"616\"}";
        logger.log("cookieGETrequest = " + cookieGETrequest);

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);
        responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);

        // 5. check if country settings roll-back was effective
        expectedCountry = "\"code\":\"616\"";
        url = "http://127.0.0.1:8080/api/s/default/get/setting/country";
        requestType = "GET";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, null,false);
        responseOutput = response.get("response");
        Assert.assertEquals(responseOutput.contains(expectedCountry),true);

        logger.log("Start of Test: Test_04_POST_SET_COUNTRY");
    }

    @Test
    /**
     * This tests ability of change of "Timezone" is possible using API POST Call
     * Steps
     * 1. Login using API call
     * 2. change "Timezone" using API call
     * 3. roll-back "Timezone" change by API Call
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_05_POST_SET_TIMEZONE() {
        logger.log("Start of Test: Test_05_POST_SET_TIMEZONE");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. change "Timezone" using API call
        String url = "http://127.0.0.1:8080/api/s/default/set/setting/locale";
        String requestType = "POST";
        String jsonString = "{\"timezone\": \"Europe/Riga\"}";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);
        String responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);
        Assert.assertEquals(responseOutput.contains("Europe/Riga"),true);

        // 3. roll-back "Timezone" change by API Call
        jsonString = "{\"timezone\": \"Europe/Warsaw\"}";
        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);
        responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);
        Assert.assertEquals(responseOutput.contains("Europe/Warsaw"),true);

        logger.log("Start of Test: Test_05_POST_SET_TIMEZONE");
    }


    @Test
    /**
     * This tests ability of executing command "set-installed" using API POST Call
     * Steps
     * 1. Login using API call
     * 2. execute command "set-installed" using API call
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_06_POST_SET_INSTALLED() {
        logger.log("Start of Test: Test_06_POST_SET_INSTALLED");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. execute command "set-installed" using API call
        String url = "http://127.0.0.1:8080/api/s/default/cmd/devmgr";
        String requestType = "POST";
        String jsonString = "{\"cmd\": \"set-installed\"}";//, \"username\": \"admin\", \"password\": \"password\"}";
        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString,false);
        String responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);
        Assert.assertEquals(responseOutput.contains("ok"),true);

        logger.log("Start of Test: Test_06_POST_SET_INSTALLED");

    }


    @Test
    /**
     * This tests ability of disabling "debug tools" using API POST Call
     * Steps
     * 1. Login using API call
     * 2. disabling "debug tools" using API call
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_07_POST_DISABLE_DEBUG_TOOLS() {
        logger.log("Start of Test: Test_07_POST_DISABLE_DEBUG_TOOLS");
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. disabling "debug tools" using API call
        String url = "http://127.0.0.1:8080/api/s/default/set/setting/mgmt";
        String requestType = "POST";
        String jsonString = "{\"debug_tools_enabled\":\"false\"}";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, jsonString, false);
        String responseOutput = response.get("response");

        logger.log("responseOutput= " + responseOutput);
        Assert.assertEquals(responseOutput.contains("\"debug_tools_enabled\":\"false\""),true);

        logger.log("End of Test: Test_07_POST_DISABLE_DEBUG_TOOLS");
    }
    @Test
    /**
     * This tests ability ofgetting self data  using API POST Call /api/self
     * Steps
     * 1. Login using API call
     * 2. GET SELF DATA using API call
     * 3. check if user with name "admin" is present in relpy "SELF"
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_09_GET_SELF_DATA() {
        logger.log("Start of Test: Test_09_GET_SELF_DATA");
        String expectedUser = "\"name\":\"admin\"";
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. GET SELF DATA using API call
        String url = "http://127.0.0.1:8080/api/self";
        String requestType = "GET";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, null,false);

        // 3. check if user with name "admin" is present in relpy "SELF"
        String responseOutput = response.get("response");
        logger.log("responseOutput= "+responseOutput);
        logger.log("responseOutput expected="+responseOutput.contains(expectedUser));
        Assert.assertEquals(responseOutput.contains(expectedUser),true);

        logger.log("End of Test: Test_09_GET_SELF_DATA");
    }


    @Test
    /**
     * This tests ability ofgetting self data  using API POST Call /api/self
     * Steps
     * 1. Login using API call
     * 2. GET Country Settings DATA using API call
     * 3. check if country is as the one set in default environment setup
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    public void Test_10_GET_COUNTRY_SETTINGS() {
        logger.log("Start of Test: Test_10_GET_COUNTRY_SETTINGS");

        String expectedCountry = "\"code\":\"616\"";
        // 1. Login using API call
        Map<String, String> response = Login_viaAPI();
        String cookieGETrequest = response.get("loginCookie");
        logger.log("cookieGETrequest = " + cookieGETrequest);

        // 2. GET Country Settings DATA using API call
        String url = "http://127.0.0.1:8080/api/s/default/get/setting/country";
        String requestType = "GET";

        response = ApiHelpers.makeApiRESTCall(url, requestType, cookieGETrequest, null,false);

        // 3. check if country is as the one set in default environment setup
        String responseOutput = response.get("response");
        Assert.assertEquals(responseOutput.contains(expectedCountry),true);

        logger.log("End of Test: Test_10_GET_COUNTRY_SETTINGS");


    }

}

