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
        System.out.println("Error during operation. Details:" +
                "\nHTTP Status Code: " + ae.getCode() + 
                "\nSEMP Error Code: " + errorInfo.getCode() + 
                "\nSEMP Error Status: " + errorInfo.getStatus() + 
                "\nSEMP Error Descriptions: " + errorInfo.getDescription());
    }

    private void initialize(String basePath, String user, String password) throws Exception {
        ApiClient thisClient = new ApiClient();
        thisClient.setBasePath(basePath + SEMP_BASE_PATH);
        thisClient.setUsername(user);
        thisClient.setPassword(password);
        sempApiInstance = new MsgVpnApi(thisClient);
    }

    private List<MsgVpnClient> getClients(String messageVpnName) throws ApiException {
        List<MsgVpnClient> cliList = new ArrayList<MsgVpnClient>();
        String cursor = null;
        do {
            MsgVpnClientsResponse resp = sempApiInstance.getMsgVpnClients(messageVpnName, LIMIT_COUNT, cursor, null, null);
            cliList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return cliList;
    }

    private List<MsgVpnClientSubscription> getClientSubscriptions(String messageVpnName, String clientName) throws ApiException {
        List<MsgVpnClientSubscription> clisubList = new ArrayList<MsgVpnClientSubscription>();
        String cursor = null;
        do {
            MsgVpnClientSubscriptionsResponse resp = sempApiInstance.getMsgVpnClientSubscriptions(messageVpnName, clientName, LIMIT_COUNT, cursor, null, null);
            clisubList.addAll(resp.getData());
            if (resp != null && resp.getMeta() != null && resp.getMeta().getPaging() != null) {
                cursor = resp.getMeta().getPaging().getCursorQuery();
            } else {
                cursor = null;
            }
        } while (cursor != null);
        return clisubList;
    }

    private void displayClientsSubscriptions(String messageVpnName) throws Exception {
        System.out.println("=== Review Topic Subscriptions for Message-VPN " + messageVpnName + " ===");
        List<MsgVpnClient> clientList = getClients(messageVpnName);
        if (clientList == null) {
            throw new Exception("Can\'t fetch clients list");
        }
        System.out.println("Retrieved " + clientList.size() + " Clients");

        for (int i = 0; i < clientList.size(); i++) {
            System.out.printf("%s\n", clientList.get(i).getClientName());
            List<MsgVpnClientSubscription> clisubList = getClientSubscriptions(messageVpnName, clientList.get(i).getClientName());
            if (clisubList == null) {
                throw new Exception("Can\'t fetch topic subscriptions");
            }
            for (int j = 0; j < clisubList.size(); j++) {
                System.out.printf("    %s\n", clisubList.get(j).getSubscriptionTopic());
            }
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
        String messageVpnName = args[3];

        Subscriptions app = new Subscriptions();

        try {
            app.initialize(vmrBasePath, vmrUser, vmrPassword);
            app.displayClientsSubscriptions(messageVpnName);
        } catch (ApiException e) {
            app.handleError(e);
            System.exit(-1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }
}
