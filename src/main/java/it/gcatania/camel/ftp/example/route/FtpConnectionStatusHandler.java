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

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

/**
 * @author fbalicchia
 */
public class FtpConnectionStatusHandler {

    private volatile boolean ftpServerUp = true;

    private ProducerTemplate producer;

    private String upMessageEndpoint;

    private String downMessagesEndpoint;

    public synchronized boolean isFtpServerUp() {
        return ftpServerUp;
    }

    public void setFtpServerUp(boolean ftpServerUp) {
        if (ftpServerUp == this.ftpServerUp) {
            return;
        }
        this.ftpServerUp = ftpServerUp;
        String endpoint;
        String filePrefix;
        if (ftpServerUp) {
            endpoint = upMessageEndpoint;
            filePrefix = "up.";
        } else {
            endpoint = downMessagesEndpoint;
            filePrefix = "down.";
        }
        long timeMillis = System.currentTimeMillis();
        producer.sendBodyAndHeader(endpoint, String.valueOf(timeMillis), Exchange.FILE_NAME, filePrefix
                                                                                             + timeMillis);
    }

    public void setLocalStorePath(StringBuilder localStorePath) {
        String endpointStart = new StringBuilder("file://").append(localStorePath).append("/").toString();
        this.downMessagesEndpoint = endpointStart + "downsFtp";
        this.upMessageEndpoint = endpointStart + "upsFtp";
    }

    public void setProducer(ProducerTemplate producer) {
        this.producer = producer;
    }
}
