package com.attempt.core.dasc.models;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.omg.CORBA.portable.ResponseHandler;
import org.springframework.util.Assert;

import com.attempt.core.dasc.client.api.CallerInterceptor;

/**
 * 用于描述客户端注册信息的信息封装.
 * @author zhouyinbin
 * @date 2019年6月25日 下午1:15:11
 *
 */
public class ClientRegisterInfo {

	 /**
     * 服务接口在客户端的Class对象
     * （可能不存在）
     */
    private Class<?> interfaceClass;
    /**
     * 用于标识特定{@link #servicePath}下的操作列表。
     * 一般设置为服务端接口上的方法名称
     */
    private List<String> operationList = new ArrayList<>();
    /**
     * 标识某个服务类的路径。
     * 一般设置为服务端接口的类路径
     */
    private String servicePath;
    /**
     * DASC服务调用的客户端拦截
     */
    private CallerInterceptor interceptor;
    /**
     * 是否允许拦截器阻断消息发送
     */
    private boolean allowInterrupt;
    /**
     * 客户端对{@link DascResopnse}消费服务
     */
    private ResponseHandler responseHandlerBean;
    /**
     * 客户端对{@link DascResopnse}消费服务在Spring容器托管的实例Bean名称
     */
    private String responseHandlerBeanName;
    /**
     * 客户端对{@link DascResopnse}消费服务向Spring容器注册用的实现类信息
     */
    private Class<?> responseHandlerImplClass;
    /**
     * 使用消息队列的名称标识
     */
    private String queue = "default";
    /**
     * 对该服务回调监听消息队列流量控制。默认为不控制
     */
    private int qos = -1;
    /**
     * 对相同{@link Message#messageId}的{@link DascResopnse}消息，可以被{@link #responseHandlerBean}逻辑重复执行的调度策略
     */
    private DascResponseRetryPolicy retryPolicy;
    /**
     * 服务参数列表数据的持久化
     */
    private boolean persistent;

    /**
     * 根据一个Java接口类信息，生成{@link #servicePath}和{@link #operationList}内容
     *
     * @param interfaceClass 接口类信息
     * @author maeagle
     * @date 2016-7-30 11:05:27
     */
    public void setInterfaceClass(Class<?> interfaceClass) {
        Assert.isTrue(interfaceClass.isInterface(), "expression must be true!");
        servicePath = interfaceClass.getName();
        operationList = Arrays.stream(interfaceClass.getMethods()).map(Method::getName).collect(Collectors.toList());
        this.interfaceClass = interfaceClass;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public List<String> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<String> operationList) {
        this.operationList = operationList;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public CallerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(CallerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public boolean isAllowInterrupt() {
        return allowInterrupt;
    }

    public void setAllowInterrupt(boolean allowInterrupt) {
        this.allowInterrupt = allowInterrupt;
    }

    public ResponseHandler getResponseHandlerBean() {
        return responseHandlerBean;
    }

    /**
     * 设置{@link #responseHandlerBean}。已经作废。
     *
     * @param responseHandler {@link #responseHandlerBean}信息
     * @see #setResponseHandlerBeanName(String)
     * @see #setResponseHandlerBean(ResponseHandler)
     * @see #setResponseHandlerImplClass(Class)
     */
    @Deprecated
    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandlerBean = responseHandler;
        this.responseHandlerBeanName = null;
        this.responseHandlerImplClass = null;
    }


    public void setResponseHandlerBean(ResponseHandler responseHandlerBean) {
        this.responseHandlerBean = responseHandlerBean;
    }

    public String getResponseHandlerBeanName() {
        return responseHandlerBeanName;
    }

    public void setResponseHandlerBeanName(String responseHandlerBeanName) {
        this.responseHandlerBeanName = responseHandlerBeanName;
    }

    public Class<?> getResponseHandlerImplClass() {
        return responseHandlerImplClass;
    }

    public void setResponseHandlerImplClass(Class<?> responseHandlerImplClass) {
        this.responseHandlerImplClass = responseHandlerImplClass;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public DascResponseRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(DascResponseRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
}
