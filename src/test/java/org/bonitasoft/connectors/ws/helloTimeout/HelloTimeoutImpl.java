package org.bonitasoft.connectors.ws.helloTimeout;

import javax.jws.WebService;

@WebService(endpointInterface = "org.bonitasoft.connectors.ws.helloTimeout.HelloTimeout")
public class HelloTimeoutImpl implements HelloTimeout {

    @Override
    public String sayHi(String text) throws InterruptedException {
        System.out.println("sayHi called");
        Thread.sleep(Long.valueOf(text).intValue());
        return "Hello Timeout";
    }

}
