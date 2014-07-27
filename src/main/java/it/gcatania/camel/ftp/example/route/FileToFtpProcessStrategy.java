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
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.camel.component.file.strategy.GenericFileDeleteProcessStrategy;
import org.apache.camel.component.file.strategy.GenericFileRenameExclusiveReadLockStrategy;

/**
 * @author ilCatania
 */
public class FileToFtpProcessStrategy<T> extends GenericFileDeleteProcessStrategy<T> {

    private FtpConnectionStatusHandler handler;

    public FileToFtpProcessStrategy() {
        setExclusiveReadLockStrategy(new GenericFileRenameExclusiveReadLockStrategy<T>());
    }

    /** {@inheritDoc} */
    @Override
    public boolean begin(GenericFileOperations<T> operations, GenericFileEndpoint<T> endpoint,
                         Exchange exchange, GenericFile<T> file) throws Exception {
        return handler.isFtpServerUp() && super.begin(operations, endpoint, exchange, file);
    }

    /** {@inheritDoc} */
    @Override
    public void rollback(GenericFileOperations<T> operations, GenericFileEndpoint<T> endpoint,
                         Exchange exchange, GenericFile<T> file) throws Exception {
        handler.setFtpServerUp(false);
        super.rollback(operations, endpoint, exchange, file);
    }

    public void setHandler(FtpConnectionStatusHandler handler) {
        this.handler = handler;
    }

}
