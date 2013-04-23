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
package org.apache.sling.discovery.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration object used as a central config point for the discovery service
 * implementation
 * <p>
 * The properties are described below under.
 */
@Component(metatype = true, label="%config.name", description="%config.description")
@Service(value = { Config.class })
public class Config {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** resource used to keep instance information such as last heartbeat, properties, incoming announcements **/
    private static final String CLUSTERINSTANCES_RESOURCE = "clusterInstances";

    /** resource used to keep the currently established view **/
    private static final String ESTABLISHED_VIEW_RESOURCE = "establishedView";

    /** resource used to keep the previously established view **/
    private static final String PREVIOUS_VIEW_RESOURCE = "previousView";

    /** resource used to keep ongoing votings **/
    private static final String ONGOING_VOTING_RESOURCE = "ongoingVotings";

    /** Configure the timeout (in seconds) after which an instance is considered dead/crashed. */
    public static final long DEFAULT_HEARTBEAT_TIMEOUT = 20;
    @Property(longValue=DEFAULT_HEARTBEAT_TIMEOUT)
    public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeatTimeout";
    private long heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;

    /** Configure the interval (in seconds) according to which the heartbeats are exchanged in the topology. */
    public static final long DEFAULT_HEARTBEAT_INTERVAL = 15;
    @Property(longValue=DEFAULT_HEARTBEAT_INTERVAL)
    public static final String HEARTBEAT_INTERVAL_KEY = "heartbeatInterval";
    private long heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;

    /** URL where to join a topology, eg http://localhost:4502/libs/sling/topology/connector */
    @Property
    public static final String TOPOLOGY_CONNECTOR_URL_KEY = "topologyConnectorUrl";
    private URL topologyConnectorUrl;

    /** list of ips and/or hostnames which are allowed to connect to /libs/sling/topology/connector */
    private static final String[] DEFAULT_TOPOLOGY_CONNECTOR_WHITELIST = {"localhost","127.0.0.1"};
    @Property(value={"localhost","127.0.0.1"})
    public static final String TOPOLOGY_CONNECTOR_WHITELIST_KEY = "topologyConnectorWhitelist";
    private String[] topologyConnectorWhitelist = DEFAULT_TOPOLOGY_CONNECTOR_WHITELIST;

    /** Path of resource where to keep discovery information, e.g /var/discovery/impl */
    private static final String DEFAULT_DISCOVERY_RESOURCE_PATH = "/var/discovery/impl";
    @Property(value=DEFAULT_DISCOVERY_RESOURCE_PATH, propertyPrivate=true)
    public static final String DISCOVERY_RESOURCE_PATH_KEY = "discoveryResourcePath";
    private String discoveryResourcePath = DEFAULT_DISCOVERY_RESOURCE_PATH;

    /** Name of the repository descriptor to be taken into account for leader election:
        those instances have preference to become leader which have the corresponding descriptor value of 'false' */
    @Property
    public static final String LEADER_ELECTION_REPOSITORY_DESCRIPTOR_NAME_KEY = "leaderElectionRepositoryDescriptor";
    private String leaderElectionRepositoryDescriptor ;

    @Activate
    protected void activate(final Map<String, Object> properties) {
        logger.debug("activate: config activated.");
        configure(properties);
    }

    protected void configure(final Map<String, Object> properties) {
        this.heartbeatTimeout = PropertiesUtil.toLong(
                properties.get(HEARTBEAT_TIMEOUT_KEY),
                DEFAULT_HEARTBEAT_TIMEOUT);
        logger.debug("configure: heartbeatTimeout='{}''", this.heartbeatTimeout);
        this.heartbeatInterval = PropertiesUtil.toLong(
                properties.get(HEARTBEAT_INTERVAL_KEY),
                DEFAULT_HEARTBEAT_INTERVAL);
        logger.debug("configure: heartbeatInterval='{}''",
                this.heartbeatInterval);
        String topologyConnectorUrlStr = PropertiesUtil.toString(
                properties.get(TOPOLOGY_CONNECTOR_URL_KEY), null);
        if ( topologyConnectorUrlStr != null && topologyConnectorUrlStr.length() > 0 ) {
            try {
                this.topologyConnectorUrl = new URL(topologyConnectorUrlStr);
                logger.debug("configure: topologyConnectorUrl='{}''",
                        this.topologyConnectorUrl);
            } catch (MalformedURLException e) {
                logger.error("configure: could not set topologyConnectorUrl: " + e,
                        e);
            }
        }
        this.topologyConnectorWhitelist = PropertiesUtil.toStringArray(
                properties.get(TOPOLOGY_CONNECTOR_WHITELIST_KEY),
                DEFAULT_TOPOLOGY_CONNECTOR_WHITELIST);
        logger.debug("configure: topologyConnectorWhitelist='{}''",
                this.topologyConnectorWhitelist);

        this.discoveryResourcePath = PropertiesUtil.toString(
                properties.get(DISCOVERY_RESOURCE_PATH_KEY),
                "");
        while(this.discoveryResourcePath.endsWith("/")) {
            this.discoveryResourcePath = this.discoveryResourcePath.substring(0,
                    this.discoveryResourcePath.length()-1);
        }
        this.discoveryResourcePath = this.discoveryResourcePath + "/";
        if (this.discoveryResourcePath==null || this.discoveryResourcePath.length()<=1) {
            // if the path is empty, or /, then use the default
            this.discoveryResourcePath = DEFAULT_DISCOVERY_RESOURCE_PATH;
        }
        logger.debug("configure: discoveryResourcePath='{}''",
                this.discoveryResourcePath);

        this.leaderElectionRepositoryDescriptor = PropertiesUtil.toString(
                properties.get(LEADER_ELECTION_REPOSITORY_DESCRIPTOR_NAME_KEY),
                null);
        logger.debug("configure: leaderElectionRepositoryDescriptor='{}''",
                this.leaderElectionRepositoryDescriptor);
    }

    /**
     * Returns the timeout (in seconds) after which an instance or voting is considered invalid/timed out
     * @return the timeout (in seconds) after which an instance or voting is considered invalid/timed out
     */
    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    /**
     * Returns the interval (in seconds) in which heartbeats are sent
     * @return the interval (in seconds) in which heartbeats are sent
     */
    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * Returns the URL to which to open a topology connector - or null if no topology connector
     * is configured (default is null)
     * @return the URL to which to open a topology connector - or null if no topology connector
     * is configured
     */
    public URL getTopologyConnectorURL() {
        return topologyConnectorUrl;
    }

    /**
     * Returns a comma separated list of hostnames and/or ip addresses which are allowed as
     * remote hosts to open connections to the topology connector servlet
     * @return a comma separated list of hostnames and/or ip addresses which are allowed as
     * remote hosts to open connections to the topology connector servlet
     */
    public String[] getTopologyConnectorWhitelist() {
        return topologyConnectorWhitelist;
    }

    /**
     * Returns the resource path where cluster instance informations are stored.
     * @return the resource path where cluster instance informations are stored
     */
    public String getClusterInstancesPath() {
        return discoveryResourcePath + CLUSTERINSTANCES_RESOURCE;
    }

    /**
     * Returns the resource path where the established view is stored.
     * @return the resource path where the established view is stored
     */
    public String getEstablishedViewPath() {
        return discoveryResourcePath + ESTABLISHED_VIEW_RESOURCE;
    }

    /**
     * Returns the resource path where ongoing votings are stored.
     * @return the resource path where ongoing votings are stored
     */
    public String getOngoingVotingsPath() {
        return discoveryResourcePath + ONGOING_VOTING_RESOURCE;
    }

    /**
     * Returns the resource path where the previous view is stored.
     * @return the resource path where the previous view is stored
     */
    public String getPreviousViewPath() {
        return discoveryResourcePath + PREVIOUS_VIEW_RESOURCE;
    }

    /**
     * Returns the repository descriptor key which is to be included in the
     * cluster leader election - or null.
     * <p>
     * When set, the value (treated as a boolean) of the repository descriptor
     * is prepended to the leader election id.
     * @return the repository descriptor key which is to be included in the
     * cluster leader election - or null
     */
    public String getLeaderElectionRepositoryDescriptor() {
        return leaderElectionRepositoryDescriptor;
    }
}