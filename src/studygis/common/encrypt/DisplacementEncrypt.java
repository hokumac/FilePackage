package studygis.common.encrypt;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/8
 * @描述 基于byte位移的加密算法
 */
public class DisplacementEncrypt implements IFileEncrypt {
    private  int displacementLength=2;

    public  DisplacementEncrypt()
    {}

    public  DisplacementEncrypt(int displacementLength)
    {
        this.displacementLength =displacementLength;
    }

    /*
     *@功能描述  以字节位移的方式进行加密
      * @参数 bytes
     * @返回值 byte[]
    */
    @Override
    public byte[] EncryptBytes(byte[] bytes,int length) {
        if(displacementLength==0)
            return  bytes;
        for (int i = 0; i < length; i++) {
            bytes[i]=(byte) ((int)bytes[i]+this.displacementLength);
        }
        return  bytes;
    }

    /*
     *@功能描述  字节位移的方式进行解密
      * @参数 bytes
     * @返回值 byte[]
    */
    @Override
    public byte[] DecryptBytes(byte[] bytes,int length) {
        if(displacementLength==0)
            return  bytes;
        for (int i = 0; i < length; i++) {
            bytes[i]=(byte) ((int)bytes[i]-this.displacementLength);
        }
        return  bytes;
    }
}
