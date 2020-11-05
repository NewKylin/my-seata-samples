package io.seata.samples.tcc.dubbo.starter;

import io.seata.samples.tcc.dubbo.ApplicationKeeper;
import io.seata.samples.tcc.dubbo.service.CreateOrderTransacion;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

/**
 * @program: seata-samples
 * @description:
 * @author: buck
 * @create: 2020-10-26 17:26
 **/
public class DubboCreateOrderServiverStart {
    public static void main(String[] args) throws SQLException {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[]{"spring/seata-tcc.xml","spring/seata-dubbo-reference.xml"});
        CreateOrderTransacion createOrderService = (CreateOrderTransacion)applicationContext.getBean("createOrderService");

        createOrderService.createOrder();

        new ApplicationKeeper(applicationContext).keep();
    }
}
