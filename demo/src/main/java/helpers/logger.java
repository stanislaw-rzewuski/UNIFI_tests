/**
 * copyright 2024 RS TECHNOLOGIES SP. Z.O.O.
 */

package helpers;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class logger {

    /**
     * This method generates internal time stamp and drops a log to console
     * @param inputString - input string that is printed to console with timestamp at the beging
     */
    public static void log(String inputString){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.ms").format(new java.util.Date()).toString(); // not very elegant but works
//        STR."Today's weather is \{ feelsLike }, with a temperature of \{ temperature } degrees \{ unit }" ; // Java 21 or 22 is needed... ehhh java
        String logMessage = "LOG - " +timeStamp +" - "+inputString;
        System.out.println(logMessage);
        // to be added logfile and writing message to it
    }

    /**
     * ExtractStringREGEXP - method for extracting string using Regexp
     * this might be better written, but it does it's job well enough
     * @param inputString -  input string on which Regexp will be run
     * @param regExp - regular expression "voodoo spell" to be run on Your string - good luck :-)
     * @return results of regexp as a String
     */
    public static String ExtractStringREGEXP(String inputString,String regExp ){
//        String regExp = "csrf_token=([^;]+)";
        Pattern patternCsrf = Pattern.compile(regExp);
        Matcher matcherCsrf = patternCsrf.matcher(inputString);
        if (matcherCsrf.find()) {
            log("regexp \""+regExp+ "\" results =" +matcherCsrf.group());
            return matcherCsrf.group();
        }else{
            log("Nothing was found");
            return null;
        }

    }
}
