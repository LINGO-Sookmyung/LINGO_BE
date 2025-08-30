package Sookmyung.Lingo.common.config;


import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class HttpClientConfig {

	@Bean
	public WebClient fastApiWebClient(
		@Value("${fastapi.base-url}") String baseUrl,
		@Value("${fastapi.timeout-millis}") long timeoutMillis
	) {
		HttpClient httpClient = HttpClient.create()
			.responseTimeout(Duration.ofMillis(timeoutMillis));

		return WebClient.builder()
			.baseUrl(baseUrl)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(
				ExchangeStrategies.builder()
					.codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20MB
					.build()
			)
			.build();
	}
}