package cn.dhbin.isme.pms.domain.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class FileInfoResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;
    private String url;
}
