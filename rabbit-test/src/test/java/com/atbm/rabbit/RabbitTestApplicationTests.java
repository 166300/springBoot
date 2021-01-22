package com.atbm.rabbit;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Serializable;

@SpringBootTest
class RabbitTestApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {

        User user = new User("张三", "zhangsan@qq.com");
        //序列化成json数据
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        //给指定交换机按照指定路由键发送指定消息
        rabbitTemplate.convertAndSend("direct_exchanage","world",user);
        System.out.println("消息已发出");
    }
    @Test
    void createQueue() {//名字  是否持久   是否排他   是否自动删除
        //Queue(java.lang.String name, boolean durable, boolean exclusive, boolean autoDelete)
        Queue queue = new Queue("my-queue-01",true,false,false);
        amqpAdmin.declareQueue(queue);
        System.out.println("队列创建完成");
    }
    @Test
    void createExchange() {//名字  是否持久  是否自动删除
        //DirectExchange(java.lang.String name, boolean durable, boolean autoDelete
        DirectExchange directExchange = new DirectExchange("my-Exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        System.out.println("交换机创建完成");
    }
    @Test
    void createBinding() {
        //Binding(
        // String destination, 目的地->队列
        // DestinationType destinationType, 目的地类型->绑定交换机/队列
        // String exchange,  交换机
        // String routingKey,   路由键
        //@Nullable Map<String, Object> arguments  调整参数
        //)
        Binding binding = new Binding(
                "my-queue-01",
                Binding.DestinationType.QUEUE,
                "my-Exchange",
                "hello",
                null);
        amqpAdmin.declareBinding(binding);
        System.out.println("绑定创建完成");
    }
}
class User implements Serializable {
    private String username;
    private String email;



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User() {
    }
}