package com.attempt.core.dasc.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DASC服务调用中，对请求参数的MQ消息体数据封装
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:05:31
 *
 */
public class DascRequest implements Serializable {

	 /**
     * 调用方唯一标识
     */
    private String callerId;
    /**
     * 如果该值为0，则表示当前消息体是由客户端发送出来的
     * 如果该值为其他值，则表示当前消息体是由服务提供方转发出来的。对应的值可作为向客户端提取参数列表的依据
     */
    private long callSeq = 0;
    /**
     * 远程获取参数所用服务调用信息
     * 如果为空，则代表从消息体内获取
     */
    private RemoteInfo argsRemote;
    /**
     * 本次服务调用的参数列表数据
     * Map<providerId.序号(从0开始), 调用参数列表>
     *
     * @see com.halo.core.dasc.utils.DascUtils#MESSAGE_MULTI_ARGS_KEY
     */
    private Map<String, String> argsData;
    /**
     * 在一次DASC客户端执行过程中，对相同DASC服务调用多次的情况做记录
     * List<providerId#循环次数>或List<providerId>（依赖于argsData中的内容）
     */
    private List<String> loopCalls;
    /**
     * DASC服务在消息传递过程中，需要透传的跟踪信息
     */
    private Map<String, String> trackData;

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public long getCallSeq() {
        return callSeq;
    }

    public void setCallSeq(long callSeq) {
        this.callSeq = callSeq;
    }

    public RemoteInfo getArgsRemote() {
        return argsRemote;
    }

    public void setArgsRemote(RemoteInfo argsRemote) {
        this.argsRemote = argsRemote;
    }

    public Map<String, String> getArgsData() {
        return argsData;
    }

    public void setArgsData(Map<String, String> argsData) {
        this.argsData = argsData;
    }

    public List<String> getLoopCalls() {
        return loopCalls;
    }

    public void setLoopCalls(List<String> loopCalls) {
        this.loopCalls = loopCalls;
    }

    public Map<String, String> getTrackData() {
        return trackData;
    }

    public void setTrackData(Map<String, String> trackData) {
        this.trackData = trackData;
    }

}
