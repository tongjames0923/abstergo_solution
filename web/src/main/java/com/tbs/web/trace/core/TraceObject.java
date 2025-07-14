package com.tbs.web.trace.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * 链路追踪ID
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
public class TraceObject implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 请求对象
     */
    private HttpServletRequest request;
}
