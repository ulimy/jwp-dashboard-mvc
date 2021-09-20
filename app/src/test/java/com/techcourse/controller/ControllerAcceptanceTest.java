package com.techcourse.controller;

import nextstep.mvc.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static nextstep.web.support.MediaType.APPLICATION_FORM;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("컨트롤러 인수 테스트")
class ControllerAcceptanceTest {

    @BeforeAll
    static void setUp() throws LifecycleException {
        final Tomcat tomcat = createTomcat();
        final Context context = tomcat.addContext("/test", null);
        Tomcat.addServlet(context, "dispatcherServlet", new DispatcherServlet());
        addWebapp(tomcat);
        tomcat.start();
    }

    private static Tomcat createTomcat() {
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);
        skipBindOnInit(tomcat);
        return tomcat;
    }

    private static void skipBindOnInit(Tomcat tomcat) {
        final Connector connector = tomcat.getConnector();
        connector.setProperty("bindOnInit", "false");
    }

    private static Context addWebapp(Tomcat tomcat) {
        final String docBase = new File("webapp/").getAbsolutePath();
        return tomcat.addWebapp("/", docBase);
    }

    @Test
    void user_api_test() throws IOException {
        //given
        //when
        final HttpURLConnection httpURLConnection = connectTomcat("/api/user?account=gugu&password=password");
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        //then
        final String actual = bufferedReader.readLine();
        assertThat(actual).isEqualTo("{\"id\":1,\"account\":\"gugu\",\"password\":\"password\",\"email\":\"hkkang@woowahan.com\"}");
    }

    @Test
    void user_api_failure_when_invalid_user() throws IOException {
        //given
        //when
        final HttpURLConnection httpURLConnection = connectTomcat("/api/user?account=ggu&password=password");
        //then
        final int responseCode = httpURLConnection.getResponseCode();
        assertThat(responseCode).isEqualTo(500);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/login/view", "/logout", "/register", "/register/view"})
    void controller_get_method(String path) throws IOException {
        //given
        //when
        final HttpURLConnection httpURLConnection = connectTomcat(path);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        //then
        assertThat(bufferedReader.readLine()).isNotNull();
    }

    @Test
    void login_test() throws IOException {
        //given
        //when
        final HttpURLConnection httpURLConnection = postConnectTomcat("/login?account=gugu&password=password", APPLICATION_FORM);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        //then
        assertThat(bufferedReader.readLine()).isNotNull();
    }


    @Test
    void register_test() throws IOException {
        //given
        //when
        final HttpURLConnection httpURLConnection = postConnectTomcat("/register?account=hihi&password=password&email=hi@hi.com", APPLICATION_FORM);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        //then
        assertThat(bufferedReader.readLine()).isNotNull();
    }


    private HttpURLConnection connectTomcat(String path) throws IOException {
        final URL url = new URL("http://localhost:8081" + path);
        return (HttpURLConnection) url.openConnection();
    }

    private HttpURLConnection postConnectTomcat(String path, String contentType) throws IOException {
        final URL url = new URL("http://localhost:8081" + path);
        final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", contentType);
        return httpURLConnection;
    }
}