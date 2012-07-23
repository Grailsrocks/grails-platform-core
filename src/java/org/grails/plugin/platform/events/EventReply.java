/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugin.platform.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Stephane Maldini <smaldini@doc4web.com>
 * @version 1.0
 * @file
 * @date 30/12/11
 * @section DESCRIPTION
 * <p/>
 * [Does stuff]
 */
public class EventReply implements Serializable, Future<Object> {

    private Future<?> futureReply;
    private List<Object> values;
    private Object value;
    private int receivers;
    private boolean futureReplyLoaded = false;

    public EventReply(Object val, int receivers) {
        this.receivers = receivers;
        initValues(val);
    }

    @SuppressWarnings("unchecked")
    protected void initValues(Object val) {
        if (receivers > 1 && val instanceof Collection) {
            this.values = new ArrayList<Object>((Collection) val);
            this.value = values.get(0);
        } else if (receivers == 1) {
            this.value = val;
            this.values = new ArrayList<Object>();
            this.values.add(this.value);
        }
        this.futureReplyLoaded = true;
    }

    public EventReply(Future<?> future, int receivers) {
        this.receivers = receivers;
        this.futureReply = future;
    }

    public List<Object> getValues() throws ExecutionException, InterruptedException {
        if (!futureReplyLoaded) {
            get();
        }
        return values;
    }

    public Object getValue() throws ExecutionException, InterruptedException {
        if (!futureReplyLoaded) {
            get();
        }
        return value;
    }

    public boolean cancel(boolean b) {
        return futureReply == null || futureReply.cancel(b);
    }

    public boolean isCancelled() {
        return futureReply != null && futureReply.isCancelled();
    }

    public boolean isDone() {
        return futureReply == null || futureReply.isDone();
    }

    public int size() throws Exception{
        return receivers;
    }

    protected void setReceivers(int receivers) {
        this.receivers = receivers;
    }

    public Object get() throws InterruptedException, ExecutionException {
        Object val = futureReply == null ? value : futureReply.get();
        if (!futureReplyLoaded) {
            initValues(val);
        }
        return val;
    }

    public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        Object val = futureReply == null ? value : futureReply.get(l, timeUnit);
        if (!futureReplyLoaded) {
            initValues(val);
        }
        return val;
    }
}
