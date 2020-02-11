package studygis.common.encrypt;

import java.util.HashMap;

/**
 * @作者 study_gis@126.com
 * @日期 2020/2/9
 * @描述 静态工厂，用于获取不同的加密算法
 */
public class EncryptFactory {
    /*
     *@功能描述 获取文件加密对象
     * @参数 method
     * @返回值 studygis.common.encrypt.IFileEncrypt
     */
    public IFileEncrypt getFileEncrypt(EncryptMethod method,String pwd) {
        if(method==EncryptMethod.Displacement)
        {
            if(pwd.isEmpty()) {
                return new DisplacementEncrypt();
            }
            else
            {
                return new DisplacementEncrypt(Integer.parseInt(pwd));
            }
        }
        else
        {
            return  new AesEncrypt(pwd);
        }
    }
}
