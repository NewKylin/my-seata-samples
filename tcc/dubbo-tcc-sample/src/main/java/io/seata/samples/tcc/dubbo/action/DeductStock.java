package io.seata.samples.tcc.dubbo.action;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import io.seata.samples.dubbo.Order;

/**
 * @program: 库存扣减服务
 * @description:
 * @author: buck
 * @create: 2020-10-26 10:48
 **/
public interface DeductStock {

    /**
     * 库存预扣减
     * @param actionContext
     * @param order
     * @return
     */
    @TwoPhaseBusinessAction(name = "DeductStock" , commitMethod = "commit", rollbackMethod = "rollback")
    public boolean prepare(BusinessActionContext actionContext,
                           @BusinessActionContextParameter(paramName = "order") Order order);

    /**
     * 库存实际扣减
     *
     * @param actionContext the action context
     * @return the boolean
     */
    public boolean commit(BusinessActionContext actionContext);

    /**
     * 回滚
     *
     * @param actionContext the action context
     * @return the boolean
     */
    public boolean rollback(BusinessActionContext actionContext);
}
