/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.ws.customer;

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
