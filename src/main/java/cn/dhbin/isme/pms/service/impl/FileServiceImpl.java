package cn.dhbin.isme.pms.service.impl;

import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.pms.domain.dto.FileDto;
import cn.dhbin.isme.pms.domain.entity.SysFile;
import cn.dhbin.isme.pms.mapper.SysFileMapper;
import cn.dhbin.isme.pms.service.FileService;
import cn.dhbin.isme.pms.util.FileUploadUtils;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    // 设置支持最大上传的文件，这里是1024*1024*2=2M
    public static final Long MAX_FILE_SIZE = 2097152L;
    @Value("${file.dir}")
    private String fileDir;
    @Resource
    private SysFileMapper sysFileMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FileDto upLoadFiles(MultipartFile file) {
        //获取要上传文件的名称
        String fileName = file.getOriginalFilename();
        //如果名称为空，返回一个文件名为空的错误
        if (!StringUtils.hasText(fileName)){
            log.info("文件名不存在");
            throw new BizException(BizResponseCode.ERR_400);
        }
        // 2024/4/24/xxxx.png
        String extractFilename = FileUploadUtils.extractFilename(file);
        // D:\\files\\2024/4/24/xxxx.png
        String fileRealPath = getFileRealPath(extractFilename);
        //如果文件超过最大值，返回超出可上传最大值的错误
        if (file.getSize() > MAX_FILE_SIZE){
            throw new BizException(BizResponseCode.ERR_400, "文件过大");
        }
        //获取到后缀名
        String fileSuffix = FileNameUtil.getSuffix(fileName);
        File newFile = new File(fileDir, extractFilename);
        if (!newFile.getParentFile().exists()){
            boolean mkdirs = newFile.getParentFile().mkdirs();
        }
        try {
            //文件写入
            file.transferTo(newFile);
        } catch (IOException e) {
            log.error("文件写入失败: path={}", fileRealPath, e);
            throw new BizException(BizResponseCode.ERR_500, "文件写入失败");
        }
        //将这些文件的信息写入到数据库中
        SysFile sysFile = new SysFile();
        sysFile.setId(IdUtil.getSnowflakeNextId());
        sysFile.setFileName(fileName);
        sysFile.setFilePath(extractFilename);
        sysFile.setFileSuffix(fileSuffix);
        sysFile.setCreateTime(LocalDateTime.now());
        sysFileMapper.insert(sysFile);
        return sysFile.convert(FileDto.class);
    }

    //根据id获取文件信息
    @Override
    public FileDto getFileById(Long id) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (Objects.isNull(sysFile)) {
            throw new BizException(BizResponseCode.ERR_400, "找不到文件");
        }
        return sysFile.convert(FileDto.class);
    }

    //将文件转化为InputStream
    @Override
    public InputStream getFileInputStream(FileDto fileDto) {
        File targetFile = new File(getFileRealPath(fileDto.getFilePath()));
        try {
            return new FileInputStream(targetFile);
        } catch (FileNotFoundException e) {
            log.error("文件流构建失败");
            throw new BizException(BizResponseCode.ERR_500, "文件流构建失败");
        }
    }

    @Override
    public List<FileDto> listFile(String category) {
        return sysFileMapper
                .selectList(Wrappers.<SysFile>lambdaQuery()
                    .eq(Objects.nonNull(category), SysFile::getCategory, category))
                .stream().map(f -> f.convert(FileDto.class))
                .toList();
    }

    @Override
    public void deleteById(Long fileId) {
        FileDto fileInfo = getFileById(fileId);
        if (Objects.isNull(fileInfo)) {
            throw new BizException(BizResponseCode.ERR_400);
        }
        File newFile = new File(fileDir, fileInfo.getFilePath());
        newFile.delete();
        sysFileMapper.deleteById(fileId);
    }

    @Override
    public String getFileRealPath(String extractFilename) {
        return fileDir + File.separator + extractFilename;
    }
}