package io.seata.samples.tcc.dubbo.action.impl;

import com.alibaba.fastjson.JSON;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.saga.statelang.parser.impl.FastjsonParser;
import io.seata.samples.dubbo.Order;
import io.seata.samples.tcc.dubbo.action.DeductStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @program: seata-samples
 * @description:
 * @author: buck
 * @create: 2020-10-26 16:15
 **/
public class DeductStockImpl implements DeductStock {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeductStockImpl.class);

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 一阶段预提交
     * @param actionContext
     * @param order
     * @return
     */
    @Override
    public boolean prepare(BusinessActionContext actionContext, Order order) {


        jdbcTemplate.update("update storage_tbl set freeze_count = freeze_count + ? where commodity_code = ?", new Object[]{order.count, order.commodityCode});

        LOGGER.info(String.format("商品编码：%s 成功冻结库存数量：%d", order.commodityCode, order.count));
        return true;
    }

    /**
     * 提交事务
     * @param actionContext the action context
     * @return
     */
    @Override
    public boolean commit(BusinessActionContext actionContext) {

        Order order = JSON.toJavaObject((JSON) actionContext.getActionContext("order"),Order.class);
        if(order == null)
            return false;

        jdbcTemplate.update("update storage_tbl set count = count - ?,freeze_count = freeze_count - ? where commodity_code = ?;", new Object[]{order.count,order.count,order.commodityCode});

        LOGGER.info(String.format("商品编码：%s 成功扣减库存数量：%d", order.commodityCode, order.count));
        return true;
    }

    /**
     * 回滚
     * @param actionContext the action context
     * @return
     */
    @Override
    public boolean rollback(BusinessActionContext actionContext) {

        Order order = JSON.toJavaObject((JSON) actionContext.getActionContext("order"),Order.class);
        if(order == null)
            return false;

        jdbcTemplate.update("update storage_tbl set freeze_count = freeze_count - ? where commodity_code = ?;", new Object[]{order.count,order.commodityCode});

        LOGGER.info(String.format("商品编码：%s 回滚冻结库存数量：%d", order.commodityCode, order.count));
        return true;
    }
}
