package com.vvd.test.types;


import com.alibaba.fastjson.JSON;
import com.vvd.test.types.rule01.factory.Rule01TradeRuleFactory;
import com.vvd.test.types.rule02.factory.Rule02TradeRuleFactory;
import com.vvd.test.types.rule02.factory.Rule02TradeRuleFactory.DynamicContext;
import com.vvd.types.design.framework.link.model1.ILogicLink;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class Link01Test {

    @Resource
    public Rule01TradeRuleFactory rule01TradeRuleFactory;

    @Test
    public void test_model01_01() throws Exception {
        ILogicLink<String, DynamicContext, String> logicLink = rule01TradeRuleFactory.openLogicLink();
        String logic = logicLink.apply("123", new Rule02TradeRuleFactory.DynamicContext());
        log.info("测试结果:{}", JSON.toJSONString(logic));
    }

}
