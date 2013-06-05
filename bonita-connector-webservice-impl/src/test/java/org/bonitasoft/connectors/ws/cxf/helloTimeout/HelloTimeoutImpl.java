/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
// START SNIPPET: service
package org.bonitasoft.connectors.ws.cxf.helloTimeout;

import javax.jws.WebService;

@WebService(endpointInterface = "org.bonitasoft.connectors.ws.cxf.helloTimeout.HelloTimeout")
public class HelloTimeoutImpl implements HelloTimeout {

	
	
	 public String sayHi(String text) {
	        System.out.println("sayHi called");
	        
	        Long timeToWait = Long.parseLong(text);
	        
	        waiting(timeToWait.intValue());
	        
	        return "Hello Timeout";
	    }
	 
	 
	 public static void waiting (int n){
	        
	        long t0, t1;
	        System.out.println("Waiting : " + n + "milliseconds"); 
	        t0 =  System.currentTimeMillis();

	        do{
	            t1 = System.currentTimeMillis();
	        }
	        while (t1 - t0 < n);
	    }
  
}
// END SNIPPET: service
