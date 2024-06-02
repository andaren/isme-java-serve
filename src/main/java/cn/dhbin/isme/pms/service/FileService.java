package cn.dhbin.isme.pms.service;

import cn.dhbin.isme.pms.domain.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileService {
    /**
     * 文件上传接口
     * @param file
     * @return
     */
    FileDto upLoadFiles(MultipartFile file);

    /**
     * 根据id获取文件
     * @param id
     * @return
     */
    FileDto getFileById(Long id);

    /**
     * 根据id获取数据流
     * @param files
     * @return
     */
    InputStream getFileInputStream(FileDto files);

    List<FileDto> listFile(String category);

    void deleteById(Long fileId);

    String getFileRealPath(String extractFilename);
}
