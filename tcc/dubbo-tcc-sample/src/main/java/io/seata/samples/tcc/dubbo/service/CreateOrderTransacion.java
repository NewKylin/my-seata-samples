package io.seata.samples.tcc.dubbo.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.samples.dubbo.Order;
import io.seata.samples.tcc.dubbo.action.CreateOrderAction;
import io.seata.samples.tcc.dubbo.action.DeductStock;
import io.seata.samples.tcc.dubbo.action.impl.DeductStockImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: seata-samples
 * @description:
 * @author: buck
 * @create: 2020-10-26 10:16
 **/
public class CreateOrderTransacion  {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeductStockImpl.class);

    private JdbcTemplate jdbcTemplate;

    private DeductStock deductStock;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DeductStock getDeductStock() {
        return deductStock;
    }

    public void setDeductStock(DeductStock deductStock) {
        this.deductStock = deductStock;
    }

    /**
     * 发起创建订单的发布式事务
     * @return
     */
    @GlobalTransactional
    public boolean createOrder() throws SQLException {

        final Order order = new Order();
        order.userId = "003";
        order.commodityCode = "C00321";
        order.count = 2;
        order.money = 100;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        BusinessActionContext businessActionContext = new BusinessActionContext();
        Map<String,Object> params = new HashMap<>();
        params.put("wareCode",order.commodityCode);
        params.put("count",order.count);
        businessActionContext.setActionContext(params);
        deductStock.prepare(null,order);

        LOGGER.info("Order Service SQL: insert into order_tbl (user_id, commodity_code, count, money) values ({}, {}, {}, {})" ,order.userId ,order.commodityCode ,order.count ,order.money );




        Connection connection = jdbcTemplate.getDataSource().getConnection();
        try {

            connection.setAutoCommit(false);

            jdbcTemplate.update((connection2 -> {
                PreparedStatement pst = connection2.prepareStatement(
                        "insert into order_tbl (user_id, commodity_code, count, money) values (?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setObject(1, order.userId);
                pst.setObject(2, order.commodityCode);
                pst.setObject(3, order.count);
                pst.setObject(4, order.money);
                return pst;
            }),keyHolder);

            if(keyHolder != null)
                throw new SQLException("我自己造的错误");
            connection.commit();
        }catch (SQLException sql){
            try {
                connection.rollback();
            }catch (SQLException e){
                e.printStackTrace();
            }
            throw sql;
        }finally {
            if(connection != null){
                connection.close();
            }
        }

        order.id = keyHolder.getKey().longValue();

        LOGGER.info("Order Service End ... Created " + order);
        return true;
    }
}
