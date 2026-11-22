package com.vvd.infrastructure.dcc;

import com.vvd.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

/**
 * @author hanjiazhi
 * @description
 * @create 2026/11/22 20:23
 */
@Service
public class DCCService {

    /**
     * 降级开关 0关闭、1开启
     */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        int lastTwoDigits = Math.abs(userId.hashCode() % 100);

        // 判断是否在切量范围内
        return lastTwoDigits < Integer.parseInt(cutRange);
    }

}
