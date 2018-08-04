/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.instance;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.functions.instance.state.StateContextImpl;
import org.apache.pulsar.functions.proto.Function.FunctionDetails;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Unit test {@link ContextImpl}.
 */
public class ContextImplTest {

    private InstanceConfig config;
    private Logger logger;
    private PulsarClient client;
    private ClassLoader classLoader;
    private ContextImpl context;

    @Before
    public void setup() {
        config = new InstanceConfig();
        FunctionDetails functionDetails = FunctionDetails.newBuilder()
            .setUserConfig("")
            .build();
        config.setFunctionDetails(functionDetails);
        logger = mock(Logger.class);
        client = mock(PulsarClient.class);
        classLoader = getClass().getClassLoader();
        context = new ContextImpl(
            config,
            logger,
            client,
            classLoader,
            new ArrayList<>()
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testIncrCounterStateDisabled() {
        context.incrCounter("test-key", 10);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCounterStateDisabled() {
        context.getCounter("test-key");
    }

    @Test(expected = IllegalStateException.class)
    public void testPutStateStateDisabled() {
        context.putState("test-key", ByteBuffer.wrap("test-value".getBytes(UTF_8)));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetStateStateDisabled() {
        context.getState("test-key");
    }

    @Test
    public void testIncrCounterStateEnabled() throws Exception {
        StateContextImpl stateContext = mock(StateContextImpl.class);
        context.setStateContext(stateContext);
        context.incrCounter("test-key", 10L);
        verify(stateContext, times(1)).incr(eq("test-key"), eq(10L));
    }

    @Test
    public void testGetCounterStateEnabled() throws Exception {
        StateContextImpl stateContext = mock(StateContextImpl.class);
        context.setStateContext(stateContext);
        context.getCounter("test-key");
        verify(stateContext, times(1)).getAmount(eq("test-key"));
    }

    @Test
    public void testPutStateStateEnabled() throws Exception {
        StateContextImpl stateContext = mock(StateContextImpl.class);
        context.setStateContext(stateContext);
        ByteBuffer buffer = ByteBuffer.wrap("test-value".getBytes(UTF_8));
        context.putState("test-key", buffer);
        verify(stateContext, times(1)).put(eq("test-key"), same(buffer));
    }

    @Test
    public void testGetStateStateEnabled() throws Exception {
        StateContextImpl stateContext = mock(StateContextImpl.class);
        context.setStateContext(stateContext);
        context.getState("test-key");
        verify(stateContext, times(1)).getValue(eq("test-key"));
    }

}