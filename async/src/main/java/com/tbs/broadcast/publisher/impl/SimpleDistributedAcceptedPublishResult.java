package com.tbs.broadcast.publisher.impl;

import java.io.Serializable;

import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.IEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDistributedAcceptedPublishResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private IEvent event;
	private String publisher;
	private HandleMode handleMode;
}
