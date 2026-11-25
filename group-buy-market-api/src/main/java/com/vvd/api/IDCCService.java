package com.vvd.api;

import com.vvd.api.response.Response;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/22 21:01
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
