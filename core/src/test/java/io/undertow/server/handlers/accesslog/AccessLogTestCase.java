package io.undertow.server.handlers.accesslog;

import java.io.IOException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(DefaultServer.class)
public class AccessLogTestCase {

    private static volatile String message;

    private static final AccessLogReciever RECIEVER = new AccessLogReciever() {
        @Override
        public void logMessage(final String msg) {
            message = msg;
        }
    };

    private static final HttpHandler HELLO_HANDLER = new HttpHandler() {
        @Override
        public void handleRequest(final HttpServerExchange exchange) throws Exception {
            exchange.getResponseSender().send("Hello");
        }
    };

    @Test
    public void testRemoteAddress() throws IOException {
        DefaultServer.setRootHandler(new AccessLogHandler(HELLO_HANDLER, RECIEVER, "Remote address %a Code %s test-header %{test-header}i", DefaultAccessLogTokens.INSTANCE));
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(DefaultServer.getDefaultServerURL() + "/path");
            get.addHeader("test-header", "test-value");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(200, result.getStatusLine().getStatusCode());
            Assert.assertEquals("Hello", HttpClientUtils.readResponse(result));
            Assert.assertEquals(message, "Remote address 127.0.0.1 Code 200 test-header test-value");
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

}