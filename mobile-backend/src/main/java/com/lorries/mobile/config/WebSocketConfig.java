package com.lorries.mobile.config;

import com.lorries.mobile.websocket.LocationWebSocketHandler;
import com.lorries.mobile.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private LocationWebSocketHandler locationWebSocketHandler;

    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(locationWebSocketHandler, "/ws/location")
                .setAllowedOrigins("*");
        
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .setAllowedOrigins("*");
    }
}
