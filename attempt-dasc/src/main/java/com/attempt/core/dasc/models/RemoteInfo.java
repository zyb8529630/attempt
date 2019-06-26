package com.attempt.core.dasc.models;

import java.io.Serializable;

/**
 * DASC服务体系中，对远程同步服务配置信息的描述
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:14:11
 *
 */
public class RemoteInfo implements Serializable{

	/**
     * 服务协议类型
     */
    private String protocols;
    /**
     * 服务地址
     */
    private String address;

    public RemoteInfo(){
        // 默认构造方法
    }

    public RemoteInfo(String protocols, String address) {
        this.protocols = protocols;
        this.address = address;
    }

    public String getProtocols() {
        return protocols;
    }

    public void setProtocols(String protocols) {
        this.protocols = protocols;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
