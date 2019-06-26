package com.attempt.core.dasc.client.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.attempt.core.common.util.CommonUtils;
import com.attempt.core.common.util.PropertiesUtils;
import com.attempt.core.dasc.client.api.CallerInterceptor;
import com.attempt.core.dasc.models.ClientRegisterInfo;
import com.attempt.core.dasc.models.DascRequest;
import com.attempt.core.queue.models.Message;

/**
 * DASC服务请求消息生产者（非单例）
 * @author zhouyinbin
 * @date 2019年6月25日 下午12:38:26
 *
 */
public class RequestProducer implements InitializingBean {

	 /**
     * 日志管理.
     */
    private static Logger logger = LoggerFactory.getLogger(AsynCallAdvice.class);
    /**
     * 调用方的服务参数列表能否放入MQ中的大小（KB）
     */
    private long argsLimitSize = -1;
    /**
     * 来源系统标识
     */
    private String orginSystem = "";
    /**
     * 对DASC错误信息的记录服务
     */
    @Autowired
    private DascFaildService dascFaildService;
    /**
     * 发送DASC消息的服务
     */
    @Autowired
    private MessageSender messageSender;
    /**
     * 参数列表读取服务
     */
    @Autowired
    private ArgsReader argsReader;
    /**
     * 全局时钟服务
     */
    @Autowired
    private GlobalClockService globalClockService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isNotEmpty(PropertiesUtils.getProperty("dasc.caller.args.limit")))
            argsLimitSize = Long.parseLong(PropertiesUtils.getProperty("dasc.caller.args.limit")) * 1024;
        if (StringUtils.isNotEmpty(PropertiesUtils.getProperty("dasc.name")))
            orginSystem = PropertiesUtils.getProperty("dasc.name");
    }

    /**
     * 创建请求消息
     *
     * @param transcationCode 交易码
     * @param callId          客户端ID
     * @return 创建的消息实例
     */
    public Message<DascRequest> createMessage(String transcationCode, String callId) {
        Message<DascRequest> requestMessage = new Message<>();
        requestMessage.setOrginSystem(orginSystem);
        requestMessage.setTransactionCode(transcationCode);
        requestMessage.setMessageId(CommonUtils.uuid());
        requestMessage.setMessageBody(new DascRequest());
        requestMessage.getMessageBody().setCallerId(callId);
        return requestMessage;
    }

    /**
     * 立即发送带有{@link Message<DascRequest>} 实例的MQ消息
     * <p>
     * 并执行{@link CallerInterceptor#before(Message, Object[])} 和{@link CallerInterceptor#after(Message, Object[])}
     *
     * @param queueSource    MQ数据源名称
     * @param requestMessage 消息内容
     * @author maeagle
     * @date 2016-1-19 16 :19:28
     */
    public void sendMessageImmediately(String queueSource, Message<DascRequest> requestMessage) {

        /**
         * 提取每个providerId对应的上下文Resolver实例,并以Map<argId, ClientContextResolver>返回
         */
        Map<String, ClientRegisterInfo> registerInfoMap = buildClientRegisterInfoMap(CallCounterHolder.get(queueSource));
        /**
         * 执行拦截器的before
         * 异常会被捕获并记录，并根据isInterruptEnabled来决定是否阻断消息发送过程
         */
        registerInfoMap.forEach((argsId, registerInfo) -> {
            CallerInterceptor interceptor = registerInfo.getInterceptor();
            if (interceptor != null) {
                try {
                    interceptor.before(requestMessage, ArgumentsHolder.get(queueSource, argsId));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    if (registerInfo.isAllowInterrupt()) {
                        //记录失败
                        String faildId = null;
                        try {
                            faildId = dascFaildService.onFaild(requestMessage,
                                    "*", queueSource, EventType.SEND_REQUEST);
                        } catch (Exception e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                        /**
                         * 如果需要抛出异常，则优先记录发送时间，并在requestMessage中设置trackInfo信息
                         */
                        TrackInfoHolder.recordSendTime(queueSource, globalClockService.getCentralTime());
                        buildTrackInfo(queueSource, requestMessage);
                        throw new DascProcessException(faildId, e);
                    }
                }
            }
        });
        /**
         * 记录发送时间
         */
        TrackInfoHolder.recordSendTime(queueSource, globalClockService.getCentralTime());
        /**
         * 最后在requestMessage中设置trackInfo信息
         */
        buildTrackInfo(queueSource, requestMessage);
        /**
         * 发送消息
         */
        try {
            /**
             * 如果客户端代码体内只调用了一个DASC服务，则允许不通过CallID的exchange，直接向providerId发送消息
             */
            Map<String, Long> counterData = CallCounterHolder.get(queueSource);
            if (counterData != null && counterData.size() == 1) {
                String proividerId = counterData.keySet().stream().findFirst().orElse(null);
                messageSender.sendRequestMessage(queueSource, proividerId, Collections.singletonList(requestMessage));
            } else {
                messageSender.sendRequestMessage(queueSource, requestMessage);
            }
        } catch (Exception e) {
            //记录失败
            String faildId = null;
            try {
                faildId = dascFaildService.onFaild(requestMessage, "*", queueSource, EventType.SEND_REQUEST);
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
            }
            ArgumentsHolder.clear();
            throw new DascProcessException(faildId, e);
        }
        /**
         * 执行拦截器的after
         * 异常会被捕获并做日志记录，不会抛出
         */
        registerInfoMap.forEach((argsId, registerInfo) -> {
            CallerInterceptor interceptor = registerInfo.getInterceptor();
            if (interceptor != null) {
                try {
                    interceptor.after(requestMessage, ArgumentsHolder.get(queueSource, argsId));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 为DASC服务端提供发送多个{@link DascRequest}的方法，主要用于DASC服务端在接收到循环调用时对剩余消息的处理。
     * 需要注意的是，在此方法中，对消息的发送一致性是通过事务和消息的保障服务控制的。MQ本身已经无法保证多消息的送达一致性
     *
     * @param queueSource             MQ数据源名称
     * @param providerId              DASC服务ID
     * @param multiRequestMessageList 待发送的消息列表
     */
    public void sendMultiRequestMessages(String queueSource,
                                         String providerId,
                                         List<Message<DascRequest>> multiRequestMessageList) {
        try {
            /**
             * 记录发送时间
             */
            String sendTime = Long.toString(globalClockService.getCentralTime());
            multiRequestMessageList.forEach(loopRequestMessage -> {
                loopRequestMessage.getMessageBody().getTrackData().put(DascUtils.TRACK_SEND_TIME_FLAG, sendTime);
            });
            /**
             * 发送消息
             */
            messageSender.sendRequestMessage(queueSource, providerId, multiRequestMessageList);
            /**
             * 记录发送轨迹
             */
            multiRequestMessageList.forEach(loopRequestMessage -> {
                SpringContextHolder.get().getBeansOfType(DascTracker.class).forEach((beanName, beanService) -> {
                    Observable.just(beanService).observeOn(Schedulers.io()).subscribe(dascTracker -> {
                        /**
                         * 将交易码塞入当前线程
                         */
                        TransactionCodeHolder.set(loopRequestMessage.getTransactionCode());
                        try {
                            /**
                             * 调用Tracker
                             */
                            dascTracker.sendDascRequestMessage(loopRequestMessage,
                                    providerId,
                                    DascUtils.buildTrackInfo(loopRequestMessage.getMessageBody().getTrackData(), null));
                        } catch (Exception ignored) {
                            if (DascUtils.logger.isDebugEnabled())
                                DascUtils.logger.debug(ignored.getMessage(), ignored);
                        }
                    });
                });
            });
        } catch (Exception e) {
            multiRequestMessageList.forEach(loopRequestMessage -> {
                /**
                 * 构造带seq的providerId
                 */
                final String providerIdWithSeq = DascUtils.generateProviderIdWithSeq(providerId,
                        Long.toString(loopRequestMessage.getMessageBody().getCallSeq()));
                /**
                 * 按照DASC服务端记录日志
                 */
                DascUtils.logInboundError(loopRequestMessage.getMessageBody().getCallerId(),
                        providerId, providerIdWithSeq, e);
                /**
                 * 数据库记录失败
                 */
                String faildId = null;
                try {
                    faildId = dascFaildService.onFaild(loopRequestMessage, providerId, queueSource, EventType.SEND_REQUEST);
                } catch (Exception e1) {
                    DascUtils.logInboundError(loopRequestMessage.getMessageBody().getCallerId(),
                            providerId, providerIdWithSeq, e1);
                }
                /**
                 * 记录发送轨迹
                 */
                final String sharedFaildId = faildId;
                final Map<String, String> trackData = TrackInfoHolder.getInbound(queueSource);
                SpringContextHolder.get().getBeansOfType(DascTracker.class).forEach((beanName, beanService) -> {
                    Observable.just(beanService).observeOn(Schedulers.io()).subscribe(dascTracker -> {
                        /**
                         * 将交易码塞入当前线程
                         */
                        TransactionCodeHolder.set(loopRequestMessage.getTransactionCode());
                        try {
                            dascTracker.sendDascRequestMessage(loopRequestMessage, providerId,
                                    DascUtils.buildTrackInfo(loopRequestMessage.getMessageBody().getTrackData(),
                                            new DascProcessException(sharedFaildId, e)));
                        } catch (Exception ignored) {
                            if (DascUtils.logger.isDebugEnabled())
                                DascUtils.logger.debug(ignored.getMessage(), ignored);
                        }
                    });
                });
            });
        }
    }

    /**
     * 构建消息体中,相同服务多次调用过程做记录
     *
     * @param queueSource MQ数据源名称
     * @param messageInfo 消息体对象
     * @author maeagle
     * @date 2016-1-19 16 :19:28
     */
    public void buildMessageLoopCallData(String queueSource, Message<DascRequest> messageInfo) {
        Map<String, Long> callCounterData = CallCounterHolder.get(queueSource);
        if (callCounterData == null || callCounterData.size() == 0) {
            messageInfo.getMessageBody().setLoopCalls(null);
            return;
        }
        List<String> providerIdList = callCounterData.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(item -> DascUtils.generateLoopCallProviderId(item.getKey(), item.getValue()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(providerIdList)) {
            messageInfo.getMessageBody().setLoopCalls(null);
            return;
        }
        messageInfo.getMessageBody().setLoopCalls(providerIdList);
    }

    /**
     * 根据参数列表大小，动态选择是否将服务调用参数数据放入消息体中传递
     *
     * @param queueSource MQ数据源名称
     * @param messageInfo 消息体对象
     * @author maeagle
     * @date 2016-1-19 16 :19:28
     */
    public void buildMessageArgsData(String queueSource, Message<DascRequest> messageInfo) {
        /**
         * 服务端部分参数可能依然需要通过地址调用来获取参数
         */
        RemoteInfo remoteInfo = new RemoteInfo(PropertiesUtils.getProperty("dasc.caller.args.protocols"),
                PropertiesUtils.getProperty("dasc.caller.args.address"));
        messageInfo.getMessageBody().setArgsRemote(remoteInfo);
        /**
         * 如果{@link argsLimitSize}!= -1 则表示配置允许在argsLimitSize范围内的参数列表内容放入消息体中直接传递
         * 将{@link argsLimitSize}与参数列表数据的字节大小比较
         * 如果满足附加消息体要求，则从{@link ArgumentsHolder}中获取所有调用的参数列表添加到消息体中
         */
        if (argsLimitSize == -1 ||
                (ArgumentsHolder.getMessageContentArgsSize(queueSource) > 0
                        && ArgumentsHolder.getMessageContentArgsSize(queueSource) <= argsLimitSize)) {
            messageInfo.getMessageBody().setArgsData(ArgumentsHolder.getMessageContentArgsData(queueSource).entrySet()
                    .stream().collect(Collectors.toMap(Map.Entry::getKey,
                            item -> item.getValue() == null ? "" : JSON.toJSONString(item.getValue(),
                                    SerializerFeature.WriteClassName))));
        } else {
            logger.info("Can't attach more messageBody to DascRequest[{}]!", CallerIdHolder.get());
            logger.info("message limit size: {}", argsLimitSize);
            logger.info("actual args size: {}", ArgumentsHolder.getMessageContentArgsSize(queueSource));
        }
    }

    /**
     * 将当前线程调用的DASC跟踪信息放入消息体中传递
     *
     * @param messageInfo 消息体对象
     * @author maeagle
     * @date 2017-7-11 16:19:28
     */
    private void buildTrackInfo(String queueSource, Message<DascRequest> messageInfo) {
        Map<String, String> trackInfo = TrackInfoHolder.getOutbound(queueSource);
        if (MapUtils.isEmpty(trackInfo))
            return;
        messageInfo.getMessageBody().setTrackData(trackInfo);
    }


    /**
     * 清理线程上所有DASC请求发送的痕迹变量
     *
     * @author maeagle
     * @date 2017-11-23 16:19:28
     */
    public void clearThreadValues() {
        clearRequestThreadValues();
        GuaranteeIdHolder.clear();
        TrackInfoHolder.clear();
    }

    /**
     * 清理线程上属于DASC请求发送的变量集合
     *
     * @author maeagle
     * @date 2017-11-23 16:19:28
     */
    public void clearRequestThreadValues() {
        CallerIdHolder.clear();
        RequestHolder.clear();
        MultiRequestHolder.clear();
        CallCounterHolder.clear();
        ArgumentsHolder.clear();
    }


    /**
     * 提取每个providerId对应的客户端注册信息实例,并以Map<argId, ClientRegisterInfo>返回
     *
     * @param callCounterData 调用计数数据
     * @return Map
     * @author maeagle
     * @date 2016-1-19 16 :19:28
     */
    private Map<String, ClientRegisterInfo> buildClientRegisterInfoMap(Map<String, Long> callCounterData) {
        Map<String, ClientRegisterInfo> resolverMap = new HashMap<>();
        callCounterData.forEach((k, v) -> {
            for (long i = 0; i < v; i++)
                resolverMap.put(DascUtils.generateCallArgsId(k, Long.toString(i)), DascUtils.getClientRegisterInfo(k));
        });
        return resolverMap;
    }

    /**
     * 调用方的服务参数列表可以放入MQ中的大小，-1代表无限制。
     *
     * @param argsLimitSizeWithKB 设置的值。单位：KB
     */
    public void setArgsLimitSize(long argsLimitSizeWithKB) {
        this.argsLimitSize = argsLimitSizeWithKB * 1024;
    }

    public void setOrginSystem(String orginSystem) {
        this.orginSystem = orginSystem;
    }

}
