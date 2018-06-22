package org.bonitasoft.connectors.ws.helloTimeout;

import javax.jws.WebService;

@WebService(endpointInterface = "org.bonitasoft.connectors.ws.helloTimeout.HelloTimeout")
public class HelloTimeoutImpl implements HelloTimeout {

    @Override
    public String sayHi(String text) {
        System.out.println("sayHi called");
        waiting(((Long) Long.parseLong(text)).intValue());
        return "Hello Timeout";
    }

    private static void waiting(int n) {
        System.out.println("Waiting : " + n + " milliseconds");
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            // never mind
        }
    }
  
}
