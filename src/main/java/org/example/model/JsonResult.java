package org.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.naming.ldap.PagedResultsControl;

@Getter
@Setter
@ToString
public class JsonResult {
    private boolean ok;//是否操作成功
    private String reason;//ok=false,表示错误信息
    private Object data;//ok=true,返回给前端的业务数据

}
