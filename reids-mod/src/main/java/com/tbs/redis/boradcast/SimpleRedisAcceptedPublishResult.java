package com.tbs.redis.boradcast;

import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleRedisAcceptedPublishResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private IEvent event;
    private String publisher;
    private HandleMode handleMode;
}
