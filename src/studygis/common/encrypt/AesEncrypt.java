package studygis.common.encrypt;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 aes加密算法  这个算法当前模式下不可用，因为加密前后的文件长度有变化
 */
public class AesEncrypt implements IFileEncrypt{
    private String passWord;
    public AesEncrypt(String pwd)
    {
        passWord=pwd;
    }
    @Override
    public byte[] EncryptBytes(byte[] bytes,int length) {
        return bytes;
    }

    @Override
    public byte[] DecryptBytes(byte[] bytes,int length) {
        return bytes;
    }
}
