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

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ilCatania
 */
public class FtpRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(FtpRoute.class);

    private String ftpProtocol;

    private String ftpHost;

    private int ftpPort = -1;

    private String ftpUser;

    private String ftpPwd;

    private String ftpOutboundPath;

    private String ftpInboundPath;

    private long ftpPollingMillis;

    private String localStorePath;

    /** {@inheritDoc} */
    @Override
    public void configure() throws Exception {

        log.info("Starting routes to host: {}", ftpHost);
        String ftpUri = createFtpUri();
        String inboundUri = fullPath(ftpUri, ftpInboundPath);
        fromF(
              inboundUri + "?password=%s" + "&delay=%s"
                  // + "&timeUnit=SECONDS" //TODO seems buggy
                  + "&delete=true" + "&disconnect=true" + "&throwExceptionOnConnectFailed=true"
                  + "&pollStrategy=#fileFromFtpPollStrategy", ftpPwd, ftpPollingMillis)
            .routeId("ftpDownload").toF("file://%s/inbound", localStorePath);

        String outboundUri = fullPath(ftpUri, ftpOutboundPath);
        fromF(
              "file://%s/outbound?processStrategy=#fileToFtpProcessStrategy&pollStrategy=#fileToFtpPollStrategy",
              localStorePath).routeId("ftpUpload").toF(outboundUri + "?password=%s", ftpPwd);

        log.info("Routes started up");
    }

    private String createFtpUri() {
        StringBuilder ftpUri = new StringBuilder(ftpProtocol).append("://").append(ftpUser).append('@')
            .append(ftpHost);
        if (ftpPort != -1) {
            ftpUri.append(':').append(ftpPort);
        }
        return ftpUri.toString();
    }

    private String fullPath(String ftpUri, String subdir) {
        StringBuilder fullPath = new StringBuilder().append(ftpUri);
        if (!subdir.startsWith("/")) {
            fullPath.append("/");
        }
        return fullPath.append(subdir).toString();
    }

    public void setFtpProtocol(String ftpProtocol) {
        this.ftpProtocol = ftpProtocol;
    }

    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public void setFtpPort(String ftpPort) {
        if (ftpPort != null && !ftpPort.isEmpty()) {
            this.ftpPort = Integer.valueOf(ftpPort);
        }
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public void setFtpPwd(String ftpPwd) {
        this.ftpPwd = ftpPwd;
    }

    public void setFtpOutboundPath(String ftpOutboundPath) {
        this.ftpOutboundPath = ftpOutboundPath;
    }

    public void setFtpInboundPath(String ftpInboundPath) {
        this.ftpInboundPath = ftpInboundPath;
    }

    public void setFtpPollingSeconds(String ftpPollingSeconds) {
        if (ftpPollingSeconds != null && !ftpPollingSeconds.isEmpty()) {
            this.ftpPollingMillis = Long.valueOf(ftpPollingSeconds) * 1000;
        }
    }

    public void setLocalStorePath(String localStorePath) {
        this.localStorePath = localStorePath;
    }
}
