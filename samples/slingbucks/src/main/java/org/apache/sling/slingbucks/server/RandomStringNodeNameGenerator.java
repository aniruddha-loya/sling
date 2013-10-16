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
package org.apache.sling.slingbucks.server;

import java.util.Random;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.NodeNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Generate hard-to-guess node names for our coffee orders.
 *  
 *  Sling calls this for all "create node" requests to its
 *  POST servlet. This reacts to POST on our ORDERS_PATH, and
 *  returns a somewhat long random human readable string for the node name. 
 */
@Component (enabled=true)
@Service
public class RandomStringNodeNameGenerator implements NodeNameGenerator {

	private final String[] randomGrp1 = {"Pink", "Red", "Blue", "Green", "Black", "White"}; 
	private final String[] randomGrp2 = {"Rabbit", "Elephant", "Tiger", "Antelope", "Gazelles"}; 
	
	private static final Random random = new Random(System.currentTimeMillis());
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** @inheritDoc */
    public String getNodeName(
            SlingHttpServletRequest request, 
            String parentPath, 
            boolean requirePrefix, 
            NodeNameGenerator defaultNng) 
    {
        if(SlingbucksConstants.ORDERS_PATH.equals(parentPath)) {
            final StringBuilder name = new StringBuilder();
            name.append(randomGrp1[random.nextInt(randomGrp1.length)]);
            name.append(randomGrp2[random.nextInt(randomGrp2.length)]);
            name.append(Integer.toString(random.nextInt(1000)));
            log.debug("Called for {} parent path, node name is {}", parentPath, name.toString());
            return name.toString();
        }
        log.debug("Path does not match {}, doing nothing", SlingbucksConstants.ORDERS_PATH);
        return null;
    }
}