package linksharing.perf.worker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;

@Component
public class TimingInterceptor implements ClientHttpRequestInterceptor {

    private final Queue<Metric> metricsQueue;

    public TimingInterceptor(@Qualifier("metricsQueue") Queue<Metric> metricsQueue) {
        this.metricsQueue = metricsQueue;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long elapsed = System.currentTimeMillis() - start;
        int errorCode = response.getStatusCode().isError() ? response.getStatusCode().value() : -1;
        metricsQueue.add(new Metric(request.getURI().toString(), elapsed, errorCode));
        return response;
    }
}
