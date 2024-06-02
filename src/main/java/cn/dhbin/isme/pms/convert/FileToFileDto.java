package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.FileDto;
import cn.dhbin.isme.pms.domain.entity.SysFile;
import cn.dhbin.mapstruct.helper.core.BeanConvertMapper;
import org.mapstruct.Mapper;

import static cn.dhbin.isme.common.mapstruct.MapstructConstant.DEFAULT_COMPONENT_MODEL;

/**
 * file
 */
@Mapper(componentModel = DEFAULT_COMPONENT_MODEL)
public interface FileToFileDto extends BeanConvertMapper<SysFile, FileDto> {

}
