package ru.netology;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CardOrderTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private String appUrl;

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        // Получаем порт из системной переменной или используем по умолчанию 9999
        String port = System.getProperty("app.port", "9999");
        appUrl = "http://localhost:" + port;
        System.out.println("Using URL: " + appUrl);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Пробуем подключиться с повторными попытками
        connectWithRetry();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void connectWithRetry() {
        int attempts = 0;
        while (attempts < 5) {
            try {
                driver.get(appUrl);
                // Если дошли сюда - подключение успешно
                System.out.println("Successfully connected to " + appUrl);
                return;
            } catch (Exception e) {
                attempts++;
                System.out.println("Connection attempt " + attempts + " failed, retrying...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Failed to connect to " + appUrl + " after 5 attempts");
    }

    @Test
    void shouldSubmitFormWithValidData() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name] input")));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.sendKeys("+79270000000");

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        if (!agreement.isSelected()) {
            agreement.click();
        }

        WebElement button = driver.findElement(By.cssSelector("button.button"));
        button.click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=order-success]")));
        String actualText = success.getText().trim();
        assertEquals("Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.", actualText);
    }

    @Test
    void shouldShowErrorWithInvalidName() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name] input")));
        nameInput.sendKeys("Ivanov Ivan");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.sendKeys("+79270000000");

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        if (!agreement.isSelected()) {
            agreement.click();
        }

        WebElement button = driver.findElement(By.cssSelector("button.button"));
        button.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name].input_invalid .input__sub")));
        String actualText = error.getText().trim();
        assertEquals("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.", actualText);
    }

    @Test
    void shouldShowErrorWithInvalidPhone() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name] input")));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.sendKeys("+7927000000"); // 10 цифр вместо 11

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        if (!agreement.isSelected()) {
            agreement.click();
        }

        WebElement button = driver.findElement(By.cssSelector("button.button"));
        button.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=phone].input_invalid .input__sub")));
        String actualText = error.getText().trim();
        assertEquals("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.", actualText);
    }

    @Test
    void shouldShowErrorWithoutAgreement() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name] input")));
        nameInput.sendKeys("Иванов Иван");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.sendKeys("+79270000000");

        // НЕ кликаем чекбокс

        WebElement button = driver.findElement(By.cssSelector("button.button"));
        button.click();

        WebElement checkbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=agreement].input_invalid")));
        assertTrue(checkbox.isDisplayed());
    }

    @Test
    void shouldSubmitFormWithComplexName() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=name] input")));
        nameInput.sendKeys("Анна-Мария Петрова-Иванова");

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.sendKeys("+79270000000");

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        if (!agreement.isSelected()) {
            agreement.click();
        }

        WebElement button = driver.findElement(By.cssSelector("button.button"));
        button.click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test-id=order-success]")));
        String actualText = success.getText().trim();
        assertEquals("Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.", actualText);
    }
}