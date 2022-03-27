package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Util.WebUtil;
import org.example.Util.WebsocketConfigurator;
import org.example.dao.MessageDao;
import org.example.dao.UserDao;
import org.example.model.Message;
import org.example.model.User;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.xml.ws.WebEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServerEndpoint(value="/message",configurator = WebsocketConfigurator.class)
public class MessageEndpoint {

    //当前客户端websocket的会话
    private Session session;
    //当前登录的用户对象
    private User loginUser;

    //private static List<Session> onlineUsers = new ArrayList<>();
    //使用map结构保存所有websocket会话,判断是否相同账号重复登录,就比较方便(key: userId,  values: Session)
    private static Map<Integer,Session> onlineUsers= new HashMap<>();

    //session对象,是建立连接的客户端websocket的会话(建立连接到关闭连接就是某个客户端的一次会话)
    //这个session和登录时,使用的HttpSession是不同,但可以通过websocket session获取到httpSession
    @OnOpen
    public void onOpen(Session session) throws IOException {
        // 验证一下是否登录, 踢掉已登录的同一个账号
        //先使用websocket session获取httpsession
        HttpSession httpSession = (HttpSession) session.getUserProperties().get("HttpSession");
        //校验: 是否已登录
        User user = WebUtil.getLoginUser(httpSession);
        if(user == null){
            //没有登录: 关闭连接
            //CloseCodes设置websocket状态码
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,
                    "没有登录,不允许发送消息");
            session.close(reason);
            return;
        }
        this.loginUser = user;
        //踢掉使用相同账号登录的用户: 找到相同用户的session,然后关闭websocket session
        //如何查找相同用户的session? List<Session>是否能找到?是否方便查找
        Session preSession = onlineUsers.get(user.getId());
        if(onlineUsers.containsKey(user.getId())) {
            //所有key中是否包含用户的id
            //相同账号重复登录: 剔除上个登录用户
            CloseReason reason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,
                    "账号在别处登录");
            preSession.close(reason);
            //这里关闭的是之前登录的会话,当前建立的会话是允许访问的
        }
        //保存到成员变量,后边的事情方法可以使用session来获取信息,发送消息
        this.session = session;
        onlineUsers.put(user.getId(),session);
        //后续服务端接收到一个消息时,还需要把消息发送给所有在线用户: 需要保存所有客户端会话
        //使用什么数据结构来保存所有客户端会话?使用成员变量还是静态变量

        //建立websocket连接后,客户端要接收历史消息
        List<Message> messages = MessageDao.query(user.getLogoutTime());
        for(Message m : messages){
            //遍历历史消息,全发送到当前用户的websocket
            //先把message转换为json字符串,在发送消息
            String json = WebUtil.write(m);
            session.getBasicRemote().sendText(json);
        }
    }
    @OnClose
    public void onClose(){
        System.out.println("断开连接");
        //关闭连接: 删除map中的当前会话,记录当前用户的上次注销时间
        onlineUsers.remove(loginUser.getId());
        loginUser.setLogoutTime(new java.util.Date());
        int n = UserDao.updateLogoutTime(loginUser);
    }
    @OnError
    public void onError(Throwable t){
        t.printStackTrace();
        //出现异常,删除map中的当前会话
        onlineUsers.remove(loginUser.getId());
    }
    @OnMessage
    public void onMessage(String message) throws IOException {
        //将我们接收到的json字符串消息,转换为message对象
        Message m = WebUtil.read(message,Message.class);
        //其他字段,可以保存的user中获取

        m.setUserId(loginUser.getId());
        m.setUserNickname(loginUser.getNickname());

        System.out.println("服务端接收到消息: "+message);
        //给所有在线用户,发送消息(消息推送: 服务端主动发)
        for(Session session : onlineUsers.values()){
            // 需要保存消息到数据库,没有在线的用户,下次登录后还需要查看
            int n = MessageDao.insert(m);
            String json = WebUtil.write(m);
            session.getBasicRemote().sendText(json);// 需要调整为插入的全部字段

        }
    }
}
