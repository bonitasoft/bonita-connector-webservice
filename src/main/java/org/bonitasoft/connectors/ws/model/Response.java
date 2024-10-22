package org.bonitasoft.connectors.ws.model;

import javax.xml.transform.Source;

public class Response {
	private Integer code;

	private Source source;

	private String errorMessage;

	public Response(int code, Source source, String errorMessage) {
		super();
		this.code = code;
		this.source = source;
		this.errorMessage = errorMessage;
	}

	public int getCode() {
		return code;
	}

	public Source getSource() {
		return source;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
