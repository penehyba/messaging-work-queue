/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@ApplicationScoped
@ApplicationPath("/api")
@Path("/")
public class Frontend extends Application {
    private static final Logger log = Logger.getLogger(Frontend.class);
    private final Data data;

    @Inject
    @JMSConnectionFactory("java:global/jms/default")
    private JMSContext jmsContext;

    public Frontend() {
        this.data = new Data();
    }

    @GET
    @Path("data")
    @Produces(MediaType.APPLICATION_JSON)
    public Data getData() {
        return data;
    }

    @POST
    @Path("send-request")
    @Consumes(MediaType.APPLICATION_JSON)
    public void sendRequest(Request request) {
        log.infof("Sending %s", request);

        Queue requests = jmsContext.createQueue("requests");
        Queue responses = jmsContext.createQueue("responses");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage();

        try {
            message.setText(request.getText());
            message.setJMSReplyTo(responses);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        producer.send(requests, message);
    }
}
