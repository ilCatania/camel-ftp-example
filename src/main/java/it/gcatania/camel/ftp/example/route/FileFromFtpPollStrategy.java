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
package it.gcatania.camel.ftp.example.route;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultPollingConsumerPollStrategy;

/**
 * @author ilCatania
 */
public class FileFromFtpPollStrategy extends DefaultPollingConsumerPollStrategy {

    private FtpConnectionStatusHandler handler;

    @Override
    public synchronized void commit(Consumer consumer, Endpoint endpoint, int polledMessages) {
        handler.setFtpServerUp(true);
    }

    @Override
    public synchronized boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter,
                                         Exception exception) throws Exception {
        log.info("Rolling back because of: {}", exception.getMessage());
        log.trace("Exception stack trace", exception);
        handler.setFtpServerUp(false);
        return false;
    }

    public void setHandler(FtpConnectionStatusHandler handler) {
        this.handler = handler;
    }
}
