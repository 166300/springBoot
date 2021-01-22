package com.atbm.rabbit.service;

import com.atbm.rabbit.bean.Order;
import com.atbm.rabbit.bean.User;
import com.rabbitmq.client.Channel;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/*
* 1,消息确认机制;
*   1). 如果这个消息收到,在处理期间,出现运行异常,会默认消息状态没有被处理
*       Unack;队列中感知到有一个这样的消息->队列会在次尝试发个其他队列
*   2). 我们不要让他认为是ack/Unack;->手动确认机制
*       否则:场景:
*           我们收到消息,并且库存扣了,但是出现未知异常,导致消息重新入队,
*           被不断地重复发送
*           (1). 手动ack
*           (2). 接口幂等性.在本地维护日志表,记录同订单下会员对商品库存的改动,再来重样的消息就不改动
* 2.手动ack
*   1). 开启手动ack->spring.rabbitmq.listener.simple.acknowledge-mode=manual
*   2).
*       public void listener(){
*           try(){
*               //处理消息,回复成功
*               channal.basicAck();
*           }catch(Exception e){
*               //处理消息,回复成功
 *              channal.basicNack/Reject(requeue:true);
*           }
*       }
* */

@Service
public class UserService {


    /*
    * 方法上可以写
    * 1.import org.springframework.amqp.core.Message;
    *       既能获取到消息内容还能获取其他消息
    * 2.User user 明确对象直接写对象就能接收
    * 3.import com.rabbitmq.client.Channel;
    *       通道
    * */
    @RabbitListener(queues = {"world"})
    public void receiveUsermessage(Message message, User user, Channel channel) throws IOException {
        //byte[] body = message.getBody();
        //MessageProperties messageProperties = message.getMessageProperties();
        System.out.println(user);

        //拒绝消息,让rabbitMQ再发给其他人
        //message.getMessageProperties().getDeliveryTag()->发消息使用的标签.
        // false不接收消息别人也不接收/true再发个别人
        channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(queues = {"order-queue"})
    public void receivieOrder(Order order,Message message,Channel channel) throws IOException {
        System.out.println("监听到新的购物订单生成++++++++"+order);
        Long skuId = order.getSkuId();
        Integer num = order.getNum();
        System.out.println("商品系统正在扣除["+skuId+"]商品数量,此次扣除["+num+"]件");
        if(num%2==0){
            System.out.println("["+skuId+"]库存扣除失败");
            //回复失败,只回复本条消息,确认返回队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            throw new RuntimeException("库存扣除失败");
        }
        System.out.println("库存扣除成功");
        //回复成功,只回复本条消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(queues = "user.order.queue")
    public void closeOrder(Order order,Channel channel,Message message) throws IOException {
        System.out.println("收到过期订单: "+order+"正在关闭订单");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }


}
