package org.example.dao;

import org.example.Util.DBUtil;
import org.example.model.Channel;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ChannelDao {

    //查询所有频道
    public static List<Channel> selectAll() {
        Connection c = null;
        Statement ps = null;
        ResultSet rs = null;
        try{
            c = DBUtil.getConnection();
            String sql = "select * from channel order by id";
            ps = c.createStatement();
            rs = ps.executeQuery(sql);
            List<Channel> channels = new ArrayList<>();
            while(rs.next()) {
                //每一行数据转换为一个Channel对象
                Channel channel = new Channel();
                int id = rs.getInt("id");
                String name = rs.getString("name");
                channel.setId(id);
                channel.setName(name);
                //添加到返回列表中
                channels.add(channel);

            }
            return channels;
        } catch (SQLException e) {
            throw new RuntimeException("查询频道列表jdbc出错",e);
        } finally {
            DBUtil.close(c,ps,rs);
        }
    }
    @Test
    public void selectAllTest(){
        System.out.println(selectAll());
    }
}
