package com.nuvoton.socketmanager;

import java.util.Map;

/**
 * Created by timsheu on 6/13/16.
 */

public interface HTTPSocketInterface {
    void httpSocketResponse(Map<String, Object> responseMap);
    void voiceConnectionOpened();
}
