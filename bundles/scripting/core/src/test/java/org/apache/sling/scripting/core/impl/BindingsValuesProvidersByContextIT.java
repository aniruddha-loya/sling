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
package org.apache.sling.scripting.core.impl;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.apache.sling.scripting.api.BindingsValuesProvidersByContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@RunWith(PaxExam.class)
public class BindingsValuesProvidersByContextIT {
    
    @Inject
    private BindingsValuesProvidersByContext bvpProvider;
    
    @Inject
    private BundleContext bundleContext;
    
    private final List<ServiceRegistration> regs = new ArrayList<ServiceRegistration>();
    
    @org.ops4j.pax.exam.Configuration
    public Option[] config() {
        final String localRepo = System.getProperty("maven.repo.local", "");

        final String bundleFileName = System.getProperty( "bundle.file.name", "BUNDLE_FILE_NOT_SET" );
        final File bundleFile = new File( bundleFileName );
        if(!bundleFile.canRead()) {
            throw new IllegalArgumentException( "Cannot read from bundle file " + bundleFile.getAbsolutePath());
        }
        
        return options(
                when( localRepo.length() > 0 ).useOptions(
                        systemProperty("org.ops4j.pax.url.mvn.localRepository").value(localRepo)
                ),
                provision(
                        bundle(bundleFile.toURI().toString()),
                        mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2"),
                        
                        mavenBundle("org.apache.sling", "org.apache.sling.scripting.api", "2.1.5-SNAPSHOT"),
                        mavenBundle("org.apache.sling", "org.apache.sling.api", "2.4.2"),
                        mavenBundle("org.apache.sling", "org.apache.sling.commons.mime", "2.1.4"),
                        mavenBundle("org.apache.sling", "org.apache.sling.commons.osgi", "2.2.0"),
                        
                        mavenBundle("org.mortbay.jetty", "servlet-api-2.5", "6.1.14")
                ),
                junitBundles()
                );
    }
    
    @Before
    public void setup() {
        regs.clear();
    }
    
    @After
    public void cleanup() {
        for(ServiceRegistration reg : regs) {
            reg.unregister();
        }
    }
    

    private Dictionary<String, Object> getProperties(String context, String engineName) {
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        if(context != null) {
            props.put(BindingsValuesProvider.CONTEXT, context.split(","));
        }
        if(engineName != null) {
            props.put(ScriptEngine.NAME, engineName);
        }
        return props;
    }

    private void addBVP(final String id, String context, String engineName) {
        final BindingsValuesProvider bvp = new BindingsValuesProvider() {
            @Override
            public String toString() {
                return id;
            }
            
            public void addBindings(Bindings b) {
            }
        };
        
        regs.add(bundleContext.registerService(BindingsValuesProvider.class.getName(), bvp, getProperties(context, engineName)));
    }
    
    private void addMap(final String id, String context, String engineName) {
        final Map<String, Object> result = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return "M_" + id;
            }
        };
        
        regs.add(bundleContext.registerService(Map.class.getName(), result, getProperties(context, engineName)));
    }
    
    private ScriptEngineFactory factory(final String engineName) {
        return new ScriptEngineFactory() {
            
            public ScriptEngine getScriptEngine() {
                return null;
            }
            
            public String getProgram(String... arg0) {
                return null;
            }
            
            public Object getParameter(String arg0) {
                return null;
            }
            
            public String getOutputStatement(String arg0) {
                return null;
            }
            
            public List<String> getNames() {
                final List<String> names = new ArrayList<String>();
                names.add(engineName);
                return names;
            }
            
            public List<String> getMimeTypes() {
                return null;
            }
            
            public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
                return null;
            }
            
            public String getLanguageVersion() {
                return null;
            }
            
            public String getLanguageName() {
                return null;
            }
            
            public List<String> getExtensions() {
                return null;
            }
            
            public String getEngineVersion() {
                return null;
            }
            
            public String getEngineName() {
                return engineName;
            }
        };
    }
    
    private String asString(Collection<?> data) {
        final StringBuilder sb = new StringBuilder();
        for(Object o : data) {
            if(sb.length() > 0) {
                sb.append(",");
            }
            sb.append(o.toString());
        }
        return sb.toString();
    }
    
    @Test
    public void testAny() {
        addBVP("one", null, "js");
        addBVP("two", null, null);
        addBVP("three", null, "*");
        addBVP("four", null, "ANY");
        addBVP("five", null, "basic");
        
        assertEquals("four,three,two,one", asString(bvpProvider.getBindingsValuesProviders(factory("js"), null)));
        assertEquals("four,three,two,five", asString(bvpProvider.getBindingsValuesProviders(factory("basic"), null)));
        assertEquals("four,three,two", asString(bvpProvider.getBindingsValuesProviders(factory("other"), null)));
    }
    
    @Test
    public void testContextsAndLanguages() {
        addBVP("foo", null, "js");
        addBVP("bar", null, null);
        addBVP("r1", "request", "js");
        addBVP("r2", "request",  null);
        addBVP("o1", "other", "js");
        addBVP("o2", "other", null);
        addBVP("o3", "other,request", null);
        addBVP("python", "python", "python");
        assertEquals("o3,r2,bar,r1,foo", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "request")));
        assertEquals("With default content", "o3,r2,bar,r1,foo", asString(bvpProvider.getBindingsValuesProviders(factory("js"), null)));
        assertEquals("o3,o2,o1", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "other")));
        assertEquals("", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "unusedContext")));
    }
    
    @Test
    public void testMapsAndBvps() {
        addBVP("foo", null, "js");
        addMap("bar", null, null);
        addMap("r1", "request", "js");
        addMap("r2", "request",  null);
        addMap("o1", "other", "js");
        addBVP("o2", "other", null);
        addMap("o3", "other,request", null);
        addMap("python", "python", "python");
        assertEquals("M_o3,M_r2,M_bar,M_r1,foo", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "request")));
        assertEquals("With default content", "M_o3,M_r2,M_bar,M_r1,foo", asString(bvpProvider.getBindingsValuesProviders(factory("js"), null)));
        assertEquals("M_o3,o2,M_o1", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "other")));
        assertEquals("", asString(bvpProvider.getBindingsValuesProviders(factory("js"), "unusedContext")));
    }
}