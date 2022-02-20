package com.github.besfian.tests;


import com.github.besfian.lombok.UserRequestData;
import com.github.besfian.lombok.UserResponseData;
import com.github.besfian.lombok.UserToken;
import com.github.besfian.models.BookListData;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static com.github.besfian.config.App.CREDENTIALS_CONFIG;
import static com.github.besfian.specs.Specs.request;
import static com.github.besfian.specs.Specs.responseSpec;
import static com.github.besfian.tests.TestData.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Owner("kgordienko")
@Tags({@Tag("Web"), @Tag("API")})
@Link(name = "Book Store", url = "https://demoqa.com/books")
@DisplayName("Тестирование веб-приложения Book Store")
public class BookStoreTest extends TestBase {

    public static final UserResponseData USER_RESPONSE_DATA = new UserResponseData();
    public static final UserRequestData USER_REQUEST_DATA = new UserRequestData();

    public static final String USER_NAME = CREDENTIALS_CONFIG.userName();
    public static final String PASSWORD = CREDENTIALS_CONFIG.password();

    public static UserRequestData setUserLoginData() {
        USER_REQUEST_DATA.setUserName(USER_NAME);
        USER_REQUEST_DATA.setPassword(PASSWORD);
        return USER_REQUEST_DATA;
    }

    @Test
    @DisplayName("Успешная генерация токена (с использованием Lombok)")
    @Tags({@Tag("Critical"), @Tag("Highest")})
    @Feature("Генерация токена")
    @Story("Метод POST /Account/v1/GenerateToken")
    @Severity(SeverityLevel.CRITICAL)
    void tokenGenerationWithLombokModelTest() {
        UserToken data = given()
                .spec(request)
                .body(setUserLoginData())
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .spec(responseSpec)
                .extract().as(UserToken.class);

        assertThat(data.getToken()).isNotNull();
        assertThat(data.getStatus()).isEqualTo("Success");
        assertThat(data.getResult()).isEqualTo("User authorized successfully.");
    }

    @Test
    @DisplayName("Отображение списка всех книг (с использованием модели)")
    @Tags({@Tag("Major"), @Tag("Medium")})
    @Feature("Список книг")
    @Story("Метод GET /BookStore/v1/Books")
    @Severity(SeverityLevel.NORMAL)
    void displayAListOfAllBooksWithModelTest() {
        BookListData data = given()
                .spec(request)
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .spec(responseSpec)
                .extract().as(BookListData.class);

        assertEquals(isbn, data.getBooks()[0].getIsbn());
        assertEquals(title, data.getBooks()[0].getTitle());
    }

    @Test
    @DisplayName("Отображение списка всех книг (с использованием Groovy)")
    @Tags({@Tag("Major"), @Tag("Medium")})
    @Feature("Список книг")
    @Story("Метод GET /BookStore/v1/Books")
    @Severity(SeverityLevel.NORMAL)
    void displayAListOfAllBooksWithGroovyTest() {
        given()
                .spec(request)
                .when()
                .get("/BookStore/v1/Books")
                .then()
                .spec(responseSpec)
                .body("books", notNullValue(),
                        "books.findAll{it.website =~/http.*?/}.website.flatten()",
                        hasItem("http://chimera.labs.oreilly.com/books/1230000000561/index.html"));
    }

    @Test
    @DisplayName("Отображение определенной книги по ISBN в списке всех книг")
    @Tags({@Tag("Major"), @Tag("Medium")})
    @Feature("Список книг")
    @Story("Метод GET /BookStore/v1/Book")
    @Severity(SeverityLevel.NORMAL)
    void displayABookByISBNInTheListOfAllBooksTest() {
        given()
                .spec(request)
                .queryParam("ISBN", isbn)
                .when()
                .get("/BookStore/v1/Book")
                .then()
                .spec(responseSpec)
                .body("isbn", is(isbn),
                        "title", is(title));
    }

    @Test
    @DisplayName("Добавление и удаление книги в профиле пользователя")
    @Tags({@Tag("Blocker"), @Tag("High")})
    @Owner("user")
    @Feature("Список книг в профиле пользователя")
    @Story("Методы POST /BookStore/v1/Books ❘ DELETE /BookStore/v1/Book")
    @Severity(SeverityLevel.BLOCKER)
    void addingAndRemovingABookInAUserProfileTest() {

/*        String addingData = "{\"userId\": \"" + USER_RESPONSE_DATA.getUserId() + "\"," +
                "\"collectionOfIsbns\" : [{\"isbn\":\"9781449325862\"}]}";*/

        given()
                .spec(request)
                .header("Authorization", "Bearer " + USER_RESPONSE_DATA.getToken())
                .body(addingData)
                .when()
                .post("/BookStore/v1/Books")
                .then()
                .log().headers().and().log().body()
                .statusCode(201)
                .body("books[0].isbn", is(isbn));

/*        String removingData = "{\"isbn\":\"9781449325862\"," +
                "\"userId\": \"" + USER_RESPONSE_DATA.getUserId() + "\"}";*/

        given()
                .spec(request)
                .header("Authorization", "Bearer " + USER_RESPONSE_DATA.getToken())
                .body(removingData)
                .when()
                .delete("/BookStore/v1/Book")
                .then()
                .log().headers().and().log().body()
                .statusCode(204)
                .body(is(""));
    }
}
