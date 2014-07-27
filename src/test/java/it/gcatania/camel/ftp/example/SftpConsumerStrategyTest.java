/**
 * Copyright 2013 Gabriele Catania <gabriele.ctn@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.gcatania.camel.ftp.example;

import java.io.IOException;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.remote.RemoteFilePollingConsumerPollStrategy;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.impl.JndiRegistry;
import org.testng.annotations.Test;

/**
 * @author fbalicchia
 */
public class SftpConsumerStrategyTest extends SftpServerTestSupport {

    private boolean fault = true;

    private final static String FTPSERVER_IS_INACTIVE = "ftpServerisInactive";

    private final static String FTPSERVER_IS_ACTIVE = "ftpServerisActive";

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("MyStrategy", new MyPollingConsumerPollStrategy());
        return registry;

    }

    @Test
    public void testSftpFaultConnection() throws Exception {
        if (!canTest()) {
            return;
        }

        fault = true;

        // create file using regular file
        template.sendBodyAndHeader("file://" + FTP_ROOT_DIR, "Ciao Mondo", Exchange.FILE_NAME, "hello.txt");

        MockEndpoint inactiveMock = getMockEndpoint("mock:inactiveResult");

        context.startRoute("foo");
        context.startRoute("inactive");
        assertMockEndpointsSatisfied();
        Thread.sleep(2000);
        inactiveMock.expectedMessageCount(1);
        inactiveMock.expectedBodiesReceived(FTPSERVER_IS_INACTIVE);
    }

    @Test
    public void testSftpWellConnection() throws Exception {
        if (!canTest()) {
            return;
        }

        fault = false;

        template.sendBodyAndHeader("file://" + FTP_ROOT_DIR, "Ciao Mondo", Exchange.FILE_NAME, "hello.txt");

        MockEndpoint activeMock = getMockEndpoint("mock:activeResult");

        context.startRoute("foo");
        context.startRoute("active");

        assertMockEndpointsSatisfied();

        Thread.sleep(2000);
        activeMock.expectedMessageCount(1);
        activeMock.expectedBodiesReceived(FTPSERVER_IS_ACTIVE);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(
                     "sftp://localhost:"
                         + getPort()
                         + "/"
                         + FTP_ROOT_DIR
                         + "?username=admin&password=admin&delay=10s&reconnectDelay=100&disconnect=true&pollStrategy=#MyStrategy")
                    .routeId("foo").autoStartup(false).to("mock:result");

                JndiRegistry registry = new JndiRegistry();
                registry.bind("MyStrategy", new MyPollingConsumerPollStrategy());

                from("direct:inactiveFtpServer").routeId("inactive").autoStartup(false)
                    .to("mock:inactiveResult");
                from("direct:activeFtpServer").routeId("active").autoStartup(false).to("mock:activeResult");

            }
        };
    }

    public class MyPollingConsumerPollStrategy extends RemoteFilePollingConsumerPollStrategy {

        @Override
        public void commit(Consumer consumer, Endpoint endpoint, int polledMessages) {

            if (fault) {
                try {
                    throw new IOException("Connection refused");
                } catch (IOException e) {
                    throw new IllegalArgumentException("Connection refused");
                }
            }

        }

        @Override
        public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception cause)
            throws Exception {
            ProducerTemplate producerTemplate = new DefaultProducerTemplate(consumer.getEndpoint()
                .getCamelContext());
            producerTemplate.sendBody("direct:inactiveFtpServer", FTPSERVER_IS_INACTIVE);
            return super.rollback(consumer, endpoint, retryCounter, cause);
        }

    }

}
