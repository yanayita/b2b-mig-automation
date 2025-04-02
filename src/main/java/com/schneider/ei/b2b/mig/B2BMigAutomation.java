package com.schneider.ei.b2b.mig;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@SpringBootApplication
public class B2BMigAutomation {

    @Value("${integrationSuite.url}")
    private String integrationSuiteUrl;
    @Value("${jsession.id}")
    private String jsessionId;
    @Value("${vcap.id}")
    private String vcapId;

    public static void main(String... args) {
        System.setProperty("java.io.tmpdir", "./temporary");
        SpringApplication.run(B2BMigAutomation.class);
    }


    @Bean
    public RestTemplate restTemplate() {
        DefaultUriBuilderFactory uriBuilder = new DefaultUriBuilderFactory();
        uriBuilder.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return new RestTemplateBuilder()
                .requestFactory(item -> requestFactory())
                .setConnectTimeout(Duration.ofMillis(60000))
                .setReadTimeout(Duration.ofMillis(60000))
                .uriTemplateHandler(uriBuilder)
                .rootUri(integrationSuiteUrl)
                .build();
    }

    private ClientHttpRequestFactory requestFactory(){
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient());
        requestFactory.setConnectTimeout(30000);
        return requestFactory;
    }

    @Bean
    public CloseableHttpClient httpClient()  {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();

        CookieStore cs = new BasicCookieStore();
        addCookie(cs, "JSESSIONID", this.jsessionId);
        addCookie(cs, "__VCAP_ID__", this.vcapId);

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cs)
                .setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE)
                .useSystemProperties()
                .build();
    }

    private void addCookie(org.apache.hc.client5.http.cookie.CookieStore cs, String key, String value) {
        BasicClientCookie cookie = new BasicClientCookie(key, value);
        try {
            cookie.setDomain(new URI(this.integrationSuiteUrl).getHost());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        cs.addCookie(cookie);
    }
}
