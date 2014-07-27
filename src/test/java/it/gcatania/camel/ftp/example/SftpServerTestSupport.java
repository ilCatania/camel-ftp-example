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

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.camel.util.ObjectHelper;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * @author fbalicchia
 */
public class SftpServerTestSupport extends BaseServerTestSupport {

    protected static final String FTP_ROOT_DIR = "target/res/home";
    protected SshServer sshd;
    protected boolean canTest;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        deleteDirectory(FTP_ROOT_DIR);
        super.setUp();

        canTest = true;
        try {
            sshd = SshServer.setUpDefaultServer();
            sshd.setPort(getPort());
            sshd.setKeyPairProvider(new FileKeyPairProvider(new String[] {"src/test/resources/hostkey.pem"}));
            sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>> asList(new SftpSubsystem.Factory()));
            sshd.setCommandFactory(new ScpCommandFactory());
            sshd.setPasswordAuthenticator(new MyPasswordAuthenticator());
            sshd.start();
        } catch (Exception e) {
            // ignore if algorithm is not on the OS
            NoSuchAlgorithmException nsae = ObjectHelper.getException(NoSuchAlgorithmException.class, e);
            if (nsae != null) {
                canTest = false;

                // warn both through console as well as log that we can not run
                // the test
                String name = System.getProperty("os.name");
                String message = nsae.getMessage();
                System.out.println("SunX509 is not avail on this platform [" + name
                                   + "] Testing is skipped! Real cause: " + message);
                log.warn("SunX509 is not avail on this platform [{}] Testing is skipped! Real cause: {}",
                         name, message);
            } else {
                // some other error then throw it so the test can fail
                throw e;
            }
        }
    }

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();

        if (sshd != null) {
            try {
                // stop asap as we may hang forever
                sshd.stop(true);
                sshd = null;
            } catch (Exception e) {
                // ignore while shutting down as we could be polling during
                // shutdown
                // and get errors when the ftp server is stopping. This is only
                // an issue
                // since we host the ftp server embedded in the same jvm for
                // unit testing
            }
        }
    }

    protected boolean canTest() {
        return canTest;
    }
}
