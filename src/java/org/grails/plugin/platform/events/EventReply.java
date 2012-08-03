/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (smaldini@vmware.com)
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

import grails.events.EventException;
import groovy.lang.Closure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Stephane Maldini <smaldini@vmware.com>
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
    private Closure onError = null;

    public void setOnError(Closure onError) {
        this.onError = onError;
    }

    public Closure getOnError() {
        return onError;
    }

    public EventReply(Object val, int receivers) {
        this.receivers = receivers;
        initValues(val);
    }

    @SuppressWarnings("unchecked")
    protected void initValues(Object val) {
        this.values = new ArrayList<Object>();

        if (receivers > 1 && val instanceof Collection) {
            this.values.addAll((Collection) val);
            this.value = values.get(0);
        } else if(receivers != 0 || val != null) {
            this.value = val;
            this.values.add(this.value);
        }
        this.futureReplyLoaded = true;
    }

    public EventReply(Future<?> future, int receivers) {
        this.receivers = receivers;
        this.futureReply = future;
    }

    protected void addValue(Object v) {
        values.add(v);
    }

    public List<Object> getValues() throws Throwable {
        if (!futureReplyLoaded) {
            get();
        }
        throwError();
        return values;
    }

    public Object getValue() throws Throwable{
        if (!futureReplyLoaded) {
            get();
        }
        throwError();
        return value;
    }

    public boolean cancel(){
        return cancel(true);
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

    public boolean isSuccess() {
        return isDone() && !hasErrors();
    }

    public List<Throwable> getErrors() {
        List<Throwable> ex = new ArrayList<Throwable>();
        if (values != null) {
            for (Object v : values) {
                if (v != null && Throwable.class.isAssignableFrom(v.getClass())) {
                    ex.add((Throwable) v);
                }
            }
        }

        return ex;
    }

    public boolean hasErrors() {
        for (Object v : values) {
            if (v != null && Throwable.class.isAssignableFrom(v.getClass())) {
                return true;
            }
        }
        return false;
    }

    public void throwError() throws Throwable{
        if(hasErrors()){
            if(onError != null){
                onError.call(this);
            }else{
                throw new EventException(getErrors().get(0));
            }
        }
    }

    public int size() throws Throwable {
        get();
        throwError();
        return receivers;
    }

    protected void setReceivers(int receivers) {
        this.receivers = receivers;
    }

    public EventReply waitFor() throws Throwable {
        get();
        throwError();
        return this;
    }

    public EventReply waitFor(long l) throws Throwable {
        get(l, TimeUnit.MILLISECONDS);
        throwError();
        return this;
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
