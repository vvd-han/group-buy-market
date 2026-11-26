package com.vvd.api;

import com.vvd.api.dto.LockMarketPayOrderRequestDTO;
import com.vvd.api.dto.LockMarketPayOrderResponseDTO;
import com.vvd.api.response.Response;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/26 22:08
 */
public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

}