package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile implements Convert {
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 文件分类
     */
    private String category;
    /**
     * 文件存储路径
     */
    @TableField("filePath")
    private String filePath;
    /**
     * 文件名称
     */
    @TableField("fileName")
    private String fileName;
    /**
     * 文件后缀名
     */
    @TableField("fileSuffix")
    private String fileSuffix;

    @TableLogic(value = "0", delval = "1")
    @TableField("deleteFlag")
    private Boolean deleteFlag;

    @TableField("createTime")
    private LocalDateTime createTime;
}
