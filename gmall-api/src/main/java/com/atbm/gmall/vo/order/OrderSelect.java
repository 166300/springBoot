package com.atbm.gmall.vo.order;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;



@Data
@ToString
public class OrderSelect implements Serializable {
    private String orderSn;//订单号
    private String receiverKeyword;//收货人
    private String status;//订单状态
    private String orderType;//订单分类
    private String sourceType;//订单来源
    private String createTime;//提交时间
    private Long pageSize = 10L ;
    private Long pageNum = 1L ;
}
