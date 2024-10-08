/**
 * copyright 2024 RS TECHNOLOGIES SP. Z.O.O.
 */

package helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ApiHelpers {

    /**
     * makeApiRESTCall - method for making API REST call - GET or POST
     * This method might be written, but it does it's job well enough
     *
     * @param inputURL - the URL for API_CALL endpoint
     * @param requestType - type of the request
     * @param cookie - cookie for request (header field will use Cookie in header) - can be null if no cookie
     * @param inputJson - JSON String that will be part of the request BODY
     * @param AuthorizationEnabled - if authorization have to be enabled  it will be Authorization field in REQUEST header - can be null. probably can be deprecated in future
     * @return Map with two string keys and two string values
     *      response - response body - usually JSON String (yes could be more JSONISH but this makes the code simpler for less-advanced in JSON
     *      responseCookie - String with cookie content from response
     *
     */
    public static Map<String, String> makeApiRESTCall(String inputURL, String requestType, String cookie, String inputJson,Boolean AuthorizationEnabled){
        Map<String,String> outputResults = new HashMap<String,String>(){{
            put("response",null);
            put("responseCookie",null);
        }};

        try {

            URL url = new URL(inputURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the request properties

            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Content-Type", "application/json;");
            connection.setRequestProperty("Accept", "*/*");
            if (cookie!=null) {
                connection.setRequestProperty("Cookie", cookie);
            }

            if (AuthorizationEnabled==true) {
                // Add Basic Authorization header
                String username = "admin";  // Replace with your username
                String password = "password";  // Replace with your password
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                String authHeaderValue = "Basic " + encodedAuth;
                connection.setRequestProperty("Authorization", authHeaderValue);
            }

            if (requestType=="POST"){

                // Define the JSON payload for the POST request
                String jsonInputString = inputJson;
                connection.setDoOutput(true);

                // Send the request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }


            } else {
                connection.setDoOutput(true);
            }

            // Get the response code (200 = success)
            int responseCode = connection.getResponseCode();
            logger.log(requestType + " response Code: " + responseCode);
            logger.log(requestType + " response Header: " + connection.getHeaderField("vary") );


            // Extract the response body
            BufferedReader in;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Success: Read response from input stream
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            } else {
                // Error: Read response from error stream
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            outputResults.replace("response", response.toString());
            // Print the response body
            logger.log("Response Body: " + response.toString());


            // Get cookies from the response
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String responseCookie : cookiesHeader) {
                    logger.log("Cookie: " + responseCookie);

                }
                outputResults.replace("responseCookie", cookiesHeader.toString());
            } else {
                logger.log("No cookies found in the response.");
                outputResults.replace("responseCookie", null);
            }

            // Handle the response (if needed)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Success, process the response if needed
                logger.log(requestType +" request succeeded.");

            } else {
                // Handle error case
                logger.log(requestType +" request failed.");
            }

            connection.disconnect();



            return outputResults;

        } catch (Exception e) {
            e.printStackTrace();
        }
        outputResults.replace("response","NOTHING HERE :-)");
        return outputResults;
    }
}
