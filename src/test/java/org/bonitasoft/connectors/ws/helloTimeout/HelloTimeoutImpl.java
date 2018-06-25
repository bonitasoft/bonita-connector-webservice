package org.bonitasoft.connectors.ws.helloTimeout;

import java.util.logging.Logger;
import javax.jws.WebService;

@WebService(endpointInterface = "org.bonitasoft.connectors.ws.helloTimeout.HelloTimeout")
public class HelloTimeoutImpl implements HelloTimeout {

    private static final Logger LOG = Logger.getLogger(HelloTimeoutImpl.class.getName());

    @Override
    public String sayHi(String text) throws InterruptedException {
        LOG.info("sayHi called");
        Thread.sleep(Long.valueOf(text).intValue());
        return "Hello Timeout";
    }

}
