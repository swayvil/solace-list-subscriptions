/*
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

package com.solace.review;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import com.solace.labs.sempclient.samplelib.ApiClient;
import com.solace.labs.sempclient.samplelib.ApiException;
import com.solace.labs.sempclient.samplelib.api.MsgVpnApi;
import com.solace.labs.sempclient.samplelib.model.*;

public class Subscriptions {
    private final String SEMP_BASE_PATH = "/SEMP/v2/monitor";
    private final int LIMIT_COUNT = 20;
    private MsgVpnApi sempApiInstance;

    private void handleError(ApiException ae) {
        Gson gson = new Gson();
        String responseString = ae.getResponseBody();
        SempMetaOnlyResponse respObj = gson.fromJson(responseString, SempMetaOnlyResponse.class);
        SempError errorInfo = respObj.getMeta().getError();
        System.out.println("Error during operation. Details:" + "\nHTTP Status Code: " + ae.getCode()
                + "\nSEMP Error Code: " + errorInfo.getCode() + "\nSEMP Error Status: " + errorInfo.getStatus()
                + "\nSEMP Error Descriptions: " + errorInfo.getDescription());
    }

    private void initialize(String basePath, String user, String password) throws Exception {
        ApiClient thisClient = new ApiClient();
        thisClient.setBasePath(basePath + SEMP_BASE_PATH);
        thisClient.setUsername(user);
        thisClient.setPassword(password);
        sempApiInstance = new MsgVpnApi(thisClient);
    }

    private List<MsgVpnClient> getClients(String msgVpnName) throws ApiException {
        List<MsgVpnClient> cliList = new ArrayList<MsgVpnClient>();
        String cursor = null;
        do {
            MsgVpnClientsResponse resp = sempApiInstance.getMsgVpnClients(msgVpnName, LIMIT_COUNT, cursor, null, null);
            cliList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return cliList;
    }

    private List<MsgVpnClientSubscription> getClientSubscriptions(String msgVpnName, String clientName)
            throws ApiException {
        List<MsgVpnClientSubscription> clisubList = new ArrayList<MsgVpnClientSubscription>();
        String cursor = null;
        do {
            MsgVpnClientSubscriptionsResponse resp = sempApiInstance.getMsgVpnClientSubscriptions(msgVpnName,
                    clientName, LIMIT_COUNT, cursor, null, null);
            clisubList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return clisubList;
    }

    private List<MsgVpnQueue> getQueues(String msgVpnName) throws ApiException {
        List<MsgVpnQueue> queueList = new ArrayList<MsgVpnQueue>();
        String cursor = null;
        do {
            MsgVpnQueuesResponse resp = sempApiInstance.getMsgVpnQueues(msgVpnName, LIMIT_COUNT, cursor, null, null);
            queueList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return queueList;
    }

    private List<MsgVpnQueueSubscription> getQueueSubscriptions(String msgVpnName, String queueName)
            throws ApiException {
        List<MsgVpnQueueSubscription> queueSubList = new ArrayList<MsgVpnQueueSubscription>();
        String cursor = null;
        do {
            MsgVpnQueueSubscriptionsResponse resp = sempApiInstance.getMsgVpnQueueSubscriptions(msgVpnName, queueName,
                    LIMIT_COUNT, cursor, null, null);
            queueSubList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return queueSubList;
    }

    private String displayClientsSubscriptions(String msgVpnName) throws Exception {
        String output = "";

        System.out.println("=== Display Topic Subscriptions for Message-VPN " + msgVpnName + " ===");
        List<MsgVpnClient> clientList = getClients(msgVpnName);
        if (clientList == null) {
            throw new Exception("Can\'t fetch clients list");
        }
        System.out.println("Retrieved " + clientList.size() + " Clients");

        for (int i = 0; i < clientList.size(); i++) {
            System.out.printf("%s\n", clientList.get(i).getClientName());
            output += clientList.get(i).getClientName() + ";" + clientList.get(i).getClientUsername() + ";";
            List<MsgVpnClientSubscription> clisubList = getClientSubscriptions(msgVpnName,
                    clientList.get(i).getClientName());
            if (clisubList == null) {
                throw new Exception("Can\'t fetch topic subscriptions");
            }
            if (clisubList.size() == 0) {
                output += "\n";
            } else {
                for (int j = 0; j < clisubList.size(); j++) {
                    if (j > 0) {
                        output += ";;";
                    }
                    output += clisubList.get(j).getSubscriptionTopic() + "\n";
                    System.out.printf("    %s\n", clisubList.get(j).getSubscriptionTopic());
                }
            }
        }
        return output;
    }

    private String displayQueuesSubscriptions(String msgVpnName) throws Exception {
        String output = "";

        System.out.println("=== Display Topic Subscriptions for Message-VPN " + msgVpnName + " queues ===");
        List<MsgVpnQueue> queueList = getQueues(msgVpnName);
        if (queueList == null) {
            throw new Exception("Can\'t fetch queues list");
        }
        System.out.println("Retrieved " + queueList.size() + " Queues");

        for (int i = 0; i < queueList.size(); i++) {
            output += queueList.get(i).getQueueName() + ";" + queueList.get(i).getOwner() + ";" + queueList.get(i).getPermission()  + ";";
            System.out.printf("%s\n", queueList.get(i).getQueueName());
            List<MsgVpnQueueSubscription> queueSubList = getQueueSubscriptions(msgVpnName,
                    queueList.get(i).getQueueName());
            if (queueSubList == null) {
                throw new Exception("Can\'t fetch topic subscriptions");
            }
            if (queueSubList.size() == 0) {
                output += "\n";
            } else {
                for (int j = 0; j < queueSubList.size(); j++) {
                    if (j > 0) {
                        output += ";;;";
                    }
                    output += queueSubList.get(j).getSubscriptionTopic() + "\n";
                    System.out.printf("    %s\n", queueSubList.get(j).getSubscriptionTopic());
                }
            }
        }
        return output;
    }

    public void writeToFile(String filename, String content) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(content);
            myWriter.close();
            System.out.println("\n=> Output written to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        final String usage = "\nUsage: solace-subscriptions-review <broker_mgmt_root_URL> <management_user> <management_password> <vpnname>";

        // Check command line arguments
        if (args.length < 4) {
            System.out.println(usage);
            System.exit(-1);
        }

        String vmrBasePath = args[0];
        String vmrUser = args[1];
        String vmrPassword = args[2];
        String msgVpnName = args[3];

        Subscriptions app = new Subscriptions();
        String output = "";

        try {
            app.initialize(vmrBasePath, vmrUser, vmrPassword);
            output = "Client name;Client username;Topic subscription\n";
            output += app.displayClientsSubscriptions(msgVpnName);
            app.writeToFile(msgVpnName + "-clients-subscriptions.csv", output);

            System.out.println("\n");
            output = "Queue name;Owner;Non-owner permission;Topic subscription\n";
            output += app.displayQueuesSubscriptions(msgVpnName);
            app.writeToFile(msgVpnName + "-queues-subscriptions.csv", output);
        } catch (ApiException e) {
            app.handleError(e);
            System.exit(-1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }
}
