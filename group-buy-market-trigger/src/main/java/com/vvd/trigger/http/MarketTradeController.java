package com.vvd.trigger.http;

import com.alibaba.fastjson.JSON;
import com.vvd.api.IMarketTradeService;
import com.vvd.api.dto.LockMarketPayOrderRequestDTO;
import com.vvd.api.dto.LockMarketPayOrderResponseDTO;
import com.vvd.api.response.Response;
import com.vvd.domain.activity.model.entity.MarketProductEntity;
import com.vvd.domain.activity.model.entity.TrialBalanceEntity;
import com.vvd.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.vvd.domain.activity.service.IIndexGroupBuyMarketService;
import com.vvd.domain.trade.model.entity.MarketPayOrderEntity;
import com.vvd.domain.trade.model.entity.PayActivityEntity;
import com.vvd.domain.trade.model.entity.PayDiscountEntity;
import com.vvd.domain.trade.model.entity.UserEntity;
import com.vvd.domain.trade.model.valobj.GroupBuyProgressVO;
import com.vvd.domain.trade.service.ITradeLockOrderService;
import com.vvd.types.enums.ResponseCode;
import com.vvd.types.exception.AppException;
import java.util.Objects;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/26 22:10
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeLockOrderService;

    @RequestMapping(value = "lock_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(
        @RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {
        try {
            if (null == lockMarketPayOrderRequestDTO) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
            }

            // 参数
            String userId = lockMarketPayOrderRequestDTO.getUserId();
            String source = lockMarketPayOrderRequestDTO.getSource();
            String channel = lockMarketPayOrderRequestDTO.getChannel();
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
            String teamId = lockMarketPayOrderRequestDTO.getTeamId();

            log.info("营销交易锁单:{} LockMarketPayOrderRequestDTO:{}", userId,
                JSON.toJSONString(lockMarketPayOrderRequestDTO));

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(channel)
                || StringUtils.isBlank(goodsId) || StringUtils.isBlank(outTradeNo) || null == activityId) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
            }
            // 查询 outTradeNo 是否已经存在交易记录
            MarketPayOrderEntity marketPayOrderEntity = tradeLockOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId,
                outTradeNo);
            if (null != marketPayOrderEntity) {
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                    .orderId(marketPayOrderEntity.getOrderId())
                    .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                    .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                    .build();

                log.info("交易锁单记录(存在):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));
                return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(lockMarketPayOrderResponseDTO)
                    .build();
            }

            // 判断拼团锁单是否完成了目标
            if (StringUtils.isNotBlank(teamId)) {
                GroupBuyProgressVO groupBuyProgressVO = tradeLockOrderService.queryGroupBuyProgress(teamId);
                if (null != groupBuyProgressVO && Objects.equals(groupBuyProgressVO.getTargetCount(),
                    groupBuyProgressVO.getLockCount())) {
                    log.info("交易锁单拦截-拼单目标已达成:{} {}", userId, teamId);
                    return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.E0012.getCode())
                        .info(ResponseCode.E0012.getInfo())
                        .build();
                }
            }

            // 营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(
                MarketProductEntity.builder()
                    .userId(userId)
                    .source(source)
                    .channel(channel)
                    .goodsId(goodsId)
                    .activityId(activityId)
                    .build());

            // 校验人群可见与可参与状态，避免空活动配置导致空指针
            if (null == trialBalanceEntity
                || !Boolean.TRUE.equals(trialBalanceEntity.getIsVisible())
                || !Boolean.TRUE.equals(trialBalanceEntity.getIsEnable())
                || null == trialBalanceEntity.getGroupBuyActivityDiscountVO()) {
                log.info("交易锁单拦截-用户不满足活动可见或参与条件:{} activityId:{}", userId, activityId);
                return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.E0002.getCode())
                    .info(ResponseCode.E0002.getInfo())
                    .build();
            }

            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

            // 锁单
            marketPayOrderEntity = tradeLockOrderService.lockMarketPayOrder(
                UserEntity.builder().userId(userId).build(),
                PayActivityEntity.builder()
                    .teamId(teamId)
                    .activityId(activityId)
                    .activityName(groupBuyActivityDiscountVO.getActivityName())
                    .startTime(groupBuyActivityDiscountVO.getStartTime())
                    .endTime(groupBuyActivityDiscountVO.getEndTime())
                    .targetCount(groupBuyActivityDiscountVO.getTarget())
                    .build(),
                PayDiscountEntity.builder()
                    .source(source)
                    .channel(channel)
                    .goodsId(goodsId)
                    .goodsName(trialBalanceEntity.getGoodsName())
                    .originalPrice(trialBalanceEntity.getOriginalPrice())
                    .deductionPrice(trialBalanceEntity.getDeductionPrice())
                    .outTradeNo(outTradeNo)
                    .build());

            log.info("交易锁单记录(新):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));

            // 返回结果
            return Response.<LockMarketPayOrderResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(LockMarketPayOrderResponseDTO.builder()
                    .orderId(marketPayOrderEntity.getOrderId())
                    .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                    .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                    .build())
                .build();
        } catch (AppException e) {
            log.error("营销交易锁单业务异常:{} LockMarketPayOrderRequestDTO:{}",
                lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                .code(e.getCode())
                .info(e.getInfo())
                .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败:{} LockMarketPayOrderRequestDTO:{}",
                lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ResponseCode.UN_ERROR.getInfo())
                .build();
        }
    }

}
