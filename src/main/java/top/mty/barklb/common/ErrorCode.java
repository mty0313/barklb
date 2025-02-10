package top.mty.barklb.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    NO_MAPPING_RECORD(30001, "没有找到节点映射记录"),
    RESP_ERROR(30002, "请求发生错误"),
    REQ_EXCEPTION(30003, "请求发生异常"),
    ALL_NODES_FAILURE(30004, "所有节点失败"),
    ;

    final int code;
    final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
