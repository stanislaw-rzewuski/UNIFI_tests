/**
 * copyright 2024 RS TECHNOLOGIES SP. Z.O.O.
 */
package Tests;

import helpers.logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.time.Duration;


public class Tests_Part_01_Setup_Network_application_via_Web_UI {
    private WebDriver browser = new ChromeDriver();
    private WebDriverWait browserWait = new WebDriverWait(browser, Duration.ofSeconds(10) );

    /**
     * This method will be executed before the Tests start.
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    @BeforeSuite
    public void initializeBrowserAndLogin() {
    // open browser and setup its position and size
    // if You run test on two monitors these lines after uncommenting might be very helpful
    //        logger.log("browser position is:" +browser.manage().window().getPosition().toString());
    //        browser.manage().window().setPosition(new Point(-1500,0));
    //        logger.log("browser position is:" +browser.manage().window().getPosition().toString());
    //        browser.manage().window().setSize(new Dimension(1280, 900));
    //        logger.log("browser size is :" +browser.manage().window().getSize().toString());

    // login
        browser.get("http://127.0.0.1:8080/manage/account/login?redirect=%2Fmanage");
        browserWait.until(ExpectedConditions.presenceOfElementLocated (By.cssSelector("[name='username']")));

        browser.findElement(By.cssSelector("[name='username']")).sendKeys("admin"); // this should be shadowed in MD5 file or other hashing function
        browser.findElement(By.cssSelector("[name='password']")).sendKeys("password"); // this should be shadowed in MD5 file or other hashing function
        browser.findElement(By.id("loginButton")).click();
        browserWait.until(ExpectedConditions.presenceOfElementLocated (By.className("css-network-39rhzn")));
    }

    /**
     * This test checks if :
     * Dashboard page -> Admin Activity widget lists admin activity with admin name specified in setup step
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    @Test
    public void TestCase_01_AdminActivity() {
        logger.log("Starting Test: TestCase_01_AdminActivity");

        browser.get("http://127.0.0.1:8080/manage/default/dashboard");
        browserWait.until(ExpectedConditions.presenceOfElementLocated (By.className("css-network-39rhzn")));

        String AdminActivity = browser.findElement(By.className("css-network-39rhzn")).getText();
        logger.log(AdminActivity);
        Assert.assertEquals(AdminActivity.contains("admin"),true);

        logger.log("End of Test: TestCase_01_AdminActivity");
    }

    /**
     * This test check is
     * Settings page -> System tab -> General section -> Country/Region dropdown value matches configuration during setup
     *
     * TODO: URL parametrization - if number of TC's will increase
     */
    @Test
    public void TestCase_02_CountryRegionCheck() {
        logger.log("Starting Test: TestCase_02_CountryRegionCheck");

        browser.get("http://127.0.0.1:8080/manage/default/dashboard");
        browserWait.until(ExpectedConditions.presenceOfElementLocated(By.className("css-network-39rhzn")));

        browser.findElement(By.cssSelector("[data-testid='navigation-settings']")).click();
        browserWait.until(ExpectedConditions.presenceOfElementLocated (By.cssSelector("[data-testid='system']")));

        browser.findElement(By.cssSelector("[data-testid='system']")).click();
        browserWait.until(ExpectedConditions.presenceOfElementLocated (By.id("country.code")));
        String CountryRegion = browser.findElement(By.id("country.code")).getAttribute("value");
        logger.log("we have found a Country/Region name equal to: " +CountryRegion);

        Assert.assertEquals(CountryRegion.contains("Poland"),true);

        logger.log("End of Test: TestCase_02_CountryRegionCheck");

    }



    /**
     * This method will be executed at the end of the test.
     */
    @AfterClass
    public void endTest() {
        browser.quit();
        browser = null;
        browserWait = null;
    }


}
