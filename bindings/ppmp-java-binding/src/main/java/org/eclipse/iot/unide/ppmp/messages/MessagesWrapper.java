/*
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.iot.unide.ppmp.messages;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.iot.unide.ppmp.commons.Device;

public class MessagesWrapper {
	@JsonProperty("content-spec")
	private final String contentSpec = "urn:spec://eclipse.org/unide/machine-message#v2";

	@JsonProperty("device")
	private Device device;

	@JsonProperty("messages")
	private List<Message> messages;

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public String getContentSpec() {
		return contentSpec;
	}
}
