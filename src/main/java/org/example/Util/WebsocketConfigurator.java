package org.example.Util;

import org.example.model.User;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class WebsocketConfigurator extends ServerEndpointConfig.Configurator {

    //进行握手阶段一些配置 : 在客户端与服务端建立微博socket连接时,
    // 服务端就可以使用这个配置来完成初始化一些操作
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //定义配置好,websocket建立连接时,先执行配置的代码,然后再执行OnOpen
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        //保存HttpSession数据
        if(httpSession != null){
            sec.getUserProperties().put("HttpSession",httpSession);
        }
        //之后OnOpen建立websocket连接,就可以通过websocket的session来获取保存HttpSession

    }
}
