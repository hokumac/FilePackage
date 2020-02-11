package studygis.common.encrypt;

/**
 * @author study_gis@126.com
 * @date 2020/2/2
 * @Decription 文件流的加密解密
 */
public interface IFileEncrypt {

/*
 *@功能描述 对字节流进行文件加密
  * @参数 bytes
 * @返回值 byte[]
*/
    byte[] EncryptBytes(byte[] bytes,int length);

    /*
     *@功能描述 文件解密
      * @参数 bytes
     * @返回值 byte[]
    */
    byte[] DecryptBytes(byte[] bytes,int length);
}
