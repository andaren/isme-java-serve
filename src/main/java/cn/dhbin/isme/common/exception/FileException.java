package cn.dhbin.isme.common.exception;

/**
 * 文件信息异常类
 */
public class FileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file");
    }

}
