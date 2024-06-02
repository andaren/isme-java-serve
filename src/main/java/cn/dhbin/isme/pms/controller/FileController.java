package cn.dhbin.isme.pms.controller;

import cn.dhbin.isme.common.auth.RoleType;
import cn.dhbin.isme.common.auth.Roles;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.R;
import cn.dhbin.isme.pms.domain.dto.FileDto;
import cn.dhbin.isme.pms.domain.response.FileInfoResponse;
import cn.dhbin.isme.pms.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件")
public class FileController {
    @Value("${file.dir}")
    private String uploadFileDir;
    @Resource
    private FileService fileService;
    @Resource
    private HttpServletRequest request;

    @GetMapping("list")
    public R<List<FileInfoResponse>> fileList(@RequestParam(value = "category", required = false) String category) {
        List<FileInfoResponse> fileList = fileService.listFile(category)
                .stream().map(fileDto -> {
                    FileInfoResponse fileInfo = new FileInfoResponse();
                    fileInfo.setUrl(getDomain(request).concat("/file/download/"+fileDto.getId()));
                    fileInfo.setFileId(fileDto.getId());
                    return fileInfo;
                })
                .toList();
        return R.ok(fileList);
    }

    @PostMapping("upload")
    @Roles(RoleType.SUPER_ADMIN)
    public R<List<FileDto>> upload(HttpServletRequest request) {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        if (!resolver.isMultipart(request)) {
            // 非文件
            throw new BizException(BizResponseCode.ERR_400, "非文件");
        }
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Iterator<String> fileNameIt = multiRequest.getFileNames();
        List<FileDto> fileInfoList = new ArrayList<>();
        while (fileNameIt.hasNext()) {
            try {
                MultipartFile file = multiRequest.getFile(fileNameIt.next());
                FileDto fileDto = fileService.upLoadFiles(file);
                fileInfoList.add(fileDto);
            } catch (Throwable e) {
                log.error("文件上传失败: msg={}", e.getMessage(), e);
            }
        }
        return R.ok(fileInfoList);
    }

    @GetMapping("/download/{fileId}")
    public void download(HttpServletResponse response, @PathVariable("fileId") Long fileId){
        FileDto fileInfo = fileService.getFileById(fileId);
        if (Objects.isNull(fileInfo)) {
            throw new BizException(BizResponseCode.ERR_400, "文件不存在");
        }
        InputStream fileInputStream = fileService.getFileInputStream(fileInfo);
        try(BufferedInputStream bis = new BufferedInputStream(fileInputStream)) {
            // prepare response
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setContentLength((int) fileInputStream.available());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileInfo.getFileName() );
            // fill content
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", fileInfo, e);
        }
    }

    @DeleteMapping("/delete/{fileId}")
    public R delete(@PathVariable("fileId") Long fileId) {
        fileService.deleteById(fileId);
        return R.ok();
    }

    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     *
     * @return 服务地址
     */
    public static String getDomain(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
