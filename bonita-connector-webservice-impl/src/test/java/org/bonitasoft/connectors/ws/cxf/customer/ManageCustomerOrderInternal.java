package org.bonitasoft.connectors.ws.cxf.customer;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal", name = "ManageCustomerOrderInternal")
public interface ManageCustomerOrderInternal {

    @WebMethod(action = "http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal/executeStep")
    @WebResult(name = "isAsync", targetNamespace = "http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal")
    public Boolean executeStep(
            @WebParam(name = "processStepId", targetNamespace = "http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal") java.lang.Long processStepId,
            @WebParam(name = "processStepDate", targetNamespace = "http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal") java.lang.String processStepDate)
            throws CreateCustomerOrderException;

}
