package io.seata.samples.tcc.dubbo.starter;

import io.seata.samples.dubbo.ApplicationKeeper;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @program: seata-samples
 * @description:
 * @author: buck
 * @create: 2020-10-26 17:23
 **/
public class DubboStockServiceStarter {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext storageContext = new ClassPathXmlApplicationContext(
                new String[]{"spring/seata-tcc.xml","spring/seata-dubbo-provider.xml"});
        new ApplicationKeeper(storageContext).keep();
    }
}
