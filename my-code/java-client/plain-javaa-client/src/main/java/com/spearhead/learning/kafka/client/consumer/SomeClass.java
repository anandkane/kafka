package com.spearhead.learning.kafka.client.consumer;

import java.util.List;

public class SomeClass {

    List<OrderDetails> getOrderDetails(List<String> orderIds){
        List<OrderDetails> listOrderDetails= null;
        for (String orderId : orderIds) {
            call back-end (orderId) // response time is 2 seconds
        }

        return listOrderDetails; }

    //--> Average number of orderIds is 10
    // --> so.. Average time to respond = 10 * 2 = 20+ seconds
}
